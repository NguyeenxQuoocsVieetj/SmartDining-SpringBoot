package org.springboot.smartdining.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.ChangePasswordRequest;
import org.springboot.smartdining.dto.EmployeeResponse;
import org.springboot.smartdining.dto.LoginRequest;
import org.springboot.smartdining.dto.MeResponse;
import org.springboot.smartdining.dto.RefreshRequest;
import org.springboot.smartdining.dto.RegisterRequest;
import org.springboot.smartdining.dto.TokenResponse;
import org.springboot.smartdining.security.EmployeePrincipal;
import org.springboot.smartdining.service.AccountService;
import org.springboot.smartdining.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AccountService accountService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(request.username(), request.password(), servletRequest.getRemoteAddr());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal EmployeePrincipal principal) {
        authService.logout(principal);
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal EmployeePrincipal principal) {
        return authService.me(principal);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal EmployeePrincipal principal,
                               @Valid @RequestBody ChangePasswordRequest request,
                               HttpServletRequest servletRequest) {
        authService.changePassword(principal, request.currentPassword(), request.newPassword(),
                servletRequest.getRemoteAddr());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse register(@Valid @RequestBody RegisterRequest request) {
        return EmployeeResponse.from(accountService.register(request));
    }
}
