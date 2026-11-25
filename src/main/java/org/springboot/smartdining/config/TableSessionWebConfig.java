package org.springboot.smartdining.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.security.TableTokens;
import org.springboot.smartdining.service.CustomerSessionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Chặn mọi API của khách tại bàn khi token không hợp lệ, trước cả khi đọc nội dung gửi lên.
 * Service vẫn kiểm lại phiên bên trong transaction của mình.
 */
@Configuration
@RequiredArgsConstructor
public class TableSessionWebConfig implements WebMvcConfigurer {

    private final CustomerSessionService customerSessionService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                customerSessionService.requireSession(request.getHeader(TableTokens.HEADER));
                return true;
            }
        }).addPathPatterns("/api/v1/table-session/**");
    }
}
