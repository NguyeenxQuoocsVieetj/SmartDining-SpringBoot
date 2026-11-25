package org.springboot.smartdining.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springboot.smartdining.security.SessionJwtConverter;
import org.springboot.smartdining.security.TokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;

/** Xác thực bằng JWT, không giữ phiên HTTP, không form đăng nhập hay basic. */
@Slf4j
@Configuration
public class SecurityConfig {

    /** HS256 cần khóa ít nhất bằng độ dài mã băm. */
    private static final int MIN_KEY_BYTES = 32;

    /**
     * Đường dẫn không cần đăng nhập. Header Authorization ở đây bị bỏ qua, để token hết hạn
     * không làm hỏng việc đăng nhập lại hay thao tác của khách.
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/public/**", "/api/v1/table-session/**", "/api/v1/auth/login", "/api/v1/auth/refresh",
            "/api/v1/auth/register", "/", "/error"};

    @Bean
    public SecretKey jwtKey(@Value("${smartdining.security.jwt-secret:}") String secret) {
        byte[] key;
        if (secret.isEmpty()) {
            key = new byte[MIN_KEY_BYTES];
            new SecureRandom().nextBytes(key);
            log.warn("Chưa đặt JWT_SECRET: dùng khóa ký ngẫu nhiên, khởi động lại là mọi phiên đăng nhập mất");
        } else {
            key = secret.getBytes(StandardCharsets.UTF_8);
            if (key.length < MIN_KEY_BYTES) {
                throw new IllegalStateException("JWT_SECRET phải dài ít nhất " + MIN_KEY_BYTES + " byte");
            }
        }
        return new SecretKeySpec(key, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtKey).macAlgorithm(MacAlgorithm.HS256).build();
        // Cùng một máy cấp và kiểm token nên không cần nới giờ
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator(TokenService.ISSUER)));
        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SessionJwtConverter sessionJwtConverter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) throws Exception {
        // Lỗi ở tầng bảo mật cũng đi qua ApiExceptionHandler để cùng định dạng ProblemDetail
        AuthenticationEntryPoint entryPoint = (HttpServletRequest request, HttpServletResponse response,
                                               org.springframework.security.core.AuthenticationException e) ->
                exceptionResolver.resolveException(request, response, null, e);
        AccessDeniedHandler deniedHandler = (request, response, e) ->
                exceptionResolver.resolveException(request, response, null, e);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/v1/auth/logout", "/api/v1/auth/me").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(server -> server
                        .bearerTokenResolver(bearerTokenResolver())
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(sessionJwtConverter)))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .build();
    }

    private static BearerTokenResolver bearerTokenResolver() {
        PathPatternRequestMatcher.Builder paths = PathPatternRequestMatcher.withDefaults();
        RequestMatcher publicRequests = new OrRequestMatcher(Arrays.stream(PUBLIC_PATHS)
                .map(path -> (RequestMatcher) paths.matcher(path))
                .toList());
        DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
        return request -> publicRequests.matches(request) ? null : defaultResolver.resolve(request);
    }
}
