package study.content.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 웹 설정 - JWT 필터 등록
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * JWT 인증 필터를 Spring에 등록
     * @return
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(jwtAuthenticationFilter);
        filterRegistrationBean.addUrlPatterns("/api/*"); // /api로 시작하는 모든 요청에 적용
        return filterRegistrationBean; // 필터 순서 설정
    }
}
