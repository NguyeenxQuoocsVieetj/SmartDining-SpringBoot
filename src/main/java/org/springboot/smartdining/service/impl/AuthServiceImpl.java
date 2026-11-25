package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.dto.MeResponse;
import org.springboot.smartdining.dto.TokenResponse;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.entity.RefreshToken;
import org.springboot.smartdining.enums.EmployeeStatus;
import org.springboot.smartdining.exception.ForbiddenException;
import org.springboot.smartdining.exception.UnauthorizedException;
import org.springboot.smartdining.mapper.CounterShiftMapper;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springboot.smartdining.mapper.RefreshTokenMapper;
import org.springboot.smartdining.security.EmployeePrincipal;
import org.springboot.smartdining.security.LoginAttemptLimiter;
import org.springboot.smartdining.security.TableTokens;
import org.springboot.smartdining.security.TokenService;
import org.springboot.smartdining.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;

/**
 * Mọi thao tác ghi phiên khóa bản ghi nhân viên trước, nên đổi token, đăng xuất, đổi mật khẩu
 * của cùng một người chạy lần lượt.
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class AuthServiceImpl implements AuthService {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,50}");

    private final EmployeeMapper employeeMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final CounterShiftMapper counterShiftMapper;
    private final PasswordChecker passwordChecker;
    private final LoginAttemptLimiter loginAttemptLimiter;
    private final TokenService tokenService;
    private final Duration sessionTimeToLive;

    public AuthServiceImpl(EmployeeMapper employeeMapper,
                           RefreshTokenMapper refreshTokenMapper,
                           CounterShiftMapper counterShiftMapper,
                           PasswordChecker passwordChecker,
                           LoginAttemptLimiter loginAttemptLimiter,
                           TokenService tokenService,
                           @Value("${smartdining.security.session-ttl:8h}") Duration sessionTimeToLive) {
        if (sessionTimeToLive.toSeconds() < 1) {
            throw new IllegalStateException("smartdining.security.session-ttl phải ít nhất 1 giây");
        }
        this.employeeMapper = employeeMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.counterShiftMapper = counterShiftMapper;
        this.passwordChecker = passwordChecker;
        this.loginAttemptLimiter = loginAttemptLimiter;
        this.tokenService = tokenService;
        this.sessionTimeToLive = sessionTimeToLive;
    }

    @Override
    public TokenResponse login(String username, String password, String clientAddress) {
        String name = require(username, "username").strip();
        require(password, "password");
        // Chuỗi không thể là username thì không tra database: bảng mã coi một số ký tự lạ bằng chữ
        // thường, sẽ lách được số đếm theo username
        boolean possible = USERNAME.matcher(name).matches();
        LoginAttemptLimiter.Attempt attempt = loginAttemptLimiter.acquire(
                possible ? name.toLowerCase(Locale.ROOT) : null, clientAddress);

        Employee found = possible ? employeeMapper.findByUsername(name) : null;
        if (!passwordChecker.matches(password, found == null ? null : found.getPasswordHash())) {
            throw invalidCredentials();
        }
        loginAttemptLimiter.succeed(attempt);

        Employee employee = employeeMapper.findByIdForUpdate(found.getId());
        // Mật khẩu có thể vừa bị đổi trong lúc đang kiểm
        if (employee == null || !Objects.equals(employee.getPasswordHash(), found.getPasswordHash())) {
            throw invalidCredentials();
        }
        requireActive(employee);

        LocalDateTime now = now();
        refreshTokenMapper.deleteExpiredByEmployee(employee.getId(), now);
        return issuePair(employee, UUID.randomUUID().toString(), now, now.plus(sessionTimeToLive));
    }

    /** Lỗi 401 vẫn được commit, để việc thu hồi phiên khi phát hiện dùng lại token không bị hủy. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, noRollbackFor = UnauthorizedException.class)
    public TokenResponse refresh(String refreshToken) {
        require(refreshToken, "refreshToken");
        RefreshToken found = refreshTokenMapper.findByTokenHash(TableTokens.hash(refreshToken));
        if (found == null) {
            throw invalidRefreshToken();
        }
        Employee employee = employeeMapper.findByIdForUpdate(found.getEmployeeId());
        // Đọc lại sau khi có khóa: request khác có thể vừa đổi chính token này
        RefreshToken current = refreshTokenMapper.findById(found.getId());
        if (employee == null || current == null) {
            throw invalidRefreshToken();
        }
        LocalDateTime now = now();
        if (current.getRevokedAt() != null || employee.getStatus() != EmployeeStatus.ACTIVE) {
            refreshTokenMapper.revokeSession(current.getSessionId(), now);
            throw invalidRefreshToken();
        }
        if (!current.getExpiresAt().isAfter(LocalDateTime.now())
                || refreshTokenMapper.revoke(current.getId(), now) == 0) {
            throw invalidRefreshToken();
        }
        return issuePair(employee, current.getSessionId(), now, current.getExpiresAt());
    }

    @Override
    public void logout(EmployeePrincipal principal) {
        employeeMapper.findByIdForUpdate(principal.id());
        refreshTokenMapper.revokeSession(principal.sessionId(), now());
    }

    @Override
    @Transactional(readOnly = true)
    public MeResponse me(EmployeePrincipal principal) {
        Employee employee = employeeMapper.findById(principal.id());
        CounterShift shift = counterShiftMapper.findOpen();
        boolean onShift = shift != null && shift.getEmployeeId().equals(principal.id());
        return new MeResponse(employee.getId(), employee.getUsername(), employee.getFullName(),
                employee.getEmail(), employee.getPhone(), employee.getAvatarUrl(), employee.getRole(),
                employee.getStatus(), onShift);
    }

    private TokenResponse issuePair(Employee employee, String sessionId, LocalDateTime createdAt,
                                    LocalDateTime sessionExpiresAt) {
        String refreshToken = TableTokens.newToken();
        refreshTokenMapper.insert(new RefreshToken(null, employee.getId(), sessionId,
                TableTokens.hash(refreshToken), createdAt, sessionExpiresAt, null));
        TokenService.AccessToken access = tokenService.issue(employee.getId(), sessionId, sessionExpiresAt);
        return new TokenResponse(access.value(), "Bearer", access.expiresInSeconds(), refreshToken,
                sessionExpiresAt, new TokenResponse.SignedInEmployee(employee.getId(), employee.getUsername(),
                        employee.getFullName(), employee.getRole()));
    }

    private static void requireActive(Employee employee) {
        if (employee.getStatus() == EmployeeStatus.PENDING) {
            throw new ForbiddenException("ACCOUNT_PENDING", "Tài khoản đang chờ quản trị duyệt");
        }
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ForbiddenException("ACCOUNT_INACTIVE", "Tài khoản đã ngưng hoạt động");
        }
    }

    private static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("INVALID_CREDENTIALS", "Sai tên đăng nhập hoặc mật khẩu");
    }

    private static UnauthorizedException invalidRefreshToken() {
        return new UnauthorizedException("INVALID_REFRESH_TOKEN", "Phiên đăng nhập đã hết, hãy đăng nhập lại");
    }
}
