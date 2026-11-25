package org.springboot.smartdining.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Objects;

/** Xác thực đã thành công; quyền là ROLE_ cộng vai trò hiện tại của nhân viên. */
public class EmployeeAuthentication extends AbstractAuthenticationToken {

    private final EmployeePrincipal principal;

    public EmployeeAuthentication(EmployeePrincipal principal) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name())));
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public EmployeePrincipal getPrincipal() {
        return principal;
    }

    /** Không giữ lại token sau khi xác thực. */
    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public String getName() {
        return principal.username();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EmployeeAuthentication that && principal.equals(that.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal);
    }
}
