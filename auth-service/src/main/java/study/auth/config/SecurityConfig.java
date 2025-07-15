package study.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean  // 추가!
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                // CSRF 비활성화 (REST API이므로)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 미사용 (JWT 기반)
                .sessionManagement(sesstion -> sesstion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 권한설정
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/**")
                        .permitAll() // auth 경로 모두 허용
                        .requestMatchers("/actuator/**") // 헬스체크 허용
                        .permitAll()
                        .anyRequest()
                        .authenticated()) // 나머지 인증 필요
                .build();
    }
}
