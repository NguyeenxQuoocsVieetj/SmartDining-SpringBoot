package org.springboot.smartdining.security;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Chữ ký và hạn của JWT đã được kiểm trước đó. Ở đây đối chiếu database ở mọi request: phiên còn
 * sống và nhân viên còn ACTIVE, nên đăng xuất, khóa tài khoản, đổi vai trò có hiệu lực ngay.
 */
@Component
@RequiredArgsConstructor
public class SessionJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final EmployeeMapper employeeMapper;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String sessionId = jwt.getClaimAsString(TokenService.SESSION_CLAIM);
        Long employeeId = employeeIdOf(jwt.getSubject());
        Employee employee = sessionId == null || employeeId == null
                ? null : employeeMapper.findActiveSession(employeeId, sessionId, LocalDateTime.now());
        if (employee == null) {
            throw new InvalidBearerTokenException("Phiên đăng nhập đã kết thúc");
        }
        return new EmployeeAuthentication(
                new EmployeePrincipal(employee.getId(), employee.getUsername(), employee.getRole(), sessionId));
    }

    private static Long employeeIdOf(String subject) {
        try {
            return subject == null ? null : Long.valueOf(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
