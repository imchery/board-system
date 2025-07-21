package study.content.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import study.common.lib.config.JwtTokenService;

import java.io.IOException;

/**
 * JWT 토큰 검증 필터
 * Authorization 헤더에서 JWT 토큰을 추출하고 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromRequest(request);

        if (token != null && jwtTokenService.validateToken(token)) {
            try {
                // JWT 토큰에서 사용자명 추출
                String username = jwtTokenService.getUsernameFromToken(token);

                // 요청에 사용자 정보 추가 (PostController에서 사용할 수 있게)
                request.setAttribute("username", username);

                log.debug("JWT token validated for user: {}", username);
            } catch (Exception e) {
                log.error("JWT token validation failed: {}", e.getMessage());
                // 토큰이 유효하지 않아도 요청은 계속 진행(에러 처리는 컨트롤러에서)
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     * "Bearer " 접두사 제거
     *
     * @param request
     * @return
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }

        return null;
    }

    /**
     * GET 요청은 JWT 검증 스킵 (조회는 누구나 가능)
     * POST, PUT, DELETE만 JWT 검증
     *
     * @param request
     * @return
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("GET".equals(method) && (
                path.contains("/like-status")   // 좋아요 상태 확인
        )) {
            return false;
        }

        // GET 요청은 JWT 검증 스킵
        if ("GET".equals(method)) {
            return true;
        }

        // 특정 경로는 JWT 검증 스킵(필요시 추가)
        if (path.startsWith("/actuator") || path.startsWith("/health")) {
            return true;
        }
        return false;
    }
}
