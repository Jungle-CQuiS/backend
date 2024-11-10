package meowKai.CQuiS_backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final String NO_CHECK_URL = "api/auth/login"; // 로그인 요청은 토큰 검증을 하지 않음

    /**
     * 1. 리프레시 토큰이 있는 경우, 리프레시 토큰이 유효하면 액세스 토큰 재발급 후 필터를 거치지 않음
     * 2. 리프레시 토큰이 없고 AccessToken만 있는 경우, 유저정보 저장 후에 필터를 계속 진행함
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        // request의 요청이 로그인 요청일 경우 토큰 검증을 하지 않음
        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 리프레시 토큰이 없거나 유효하지 않으면 null 반환
        String refreshToken = jwtService
                .extractRefreshToken(request) // request에서 refreshtoken 추출
                .filter(jwtService::isTokenValid) // 유효성 검증
                .orElse(null);

        // 리프레시 토큰이 유효한 경우 리프레시 토큰을 가진 유저 정보를 가져오고 유저가 존재하면 액세스 토큰 발급
        if (refreshToken != null) {
            checkRefreshTokenAndIssueAccessToken(response, refreshToken);
            return;
        }

        // 리프레시 토큰이 없다면 액세스 토큰을 검사함
        /**
         * 1. request에서 액세스 토큰 추출
         * 2. 추출한 액세스 토큰의 유효성 검증
         * 3. 액세스 토큰에서 이메일 추출
         * 4. 추출한 이메일로 유저 정보 조회
         * 5. 유저 정보로 인증 정보 저장
         */
        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    private void checkAccessTokenAndAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        /**
         * 추출한 액세스 토큰 유효성 검증
         * 액세스 토큰이 유효한 경우
         * 액세스 토큰에서 이메일 추출
         * 추출한 이메일로 유저 정보 조회
         * 유저 정보로 인증 정보 저장
         */
        jwtService.extractAccessToken(request) // 액세스 토큰 추출
                .filter(jwtService::isTokenValid)
                .flatMap(jwtService::extractEmail)
                .flatMap(userRepository::findByEmail)
                .ifPresent(this::saveAuthentication);

        filterChain.doFilter(request, response);
    }

    private void saveAuthentication(User user) {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, authoritiesMapper.mapAuthorities(userDetails.getAuthorities())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // 리프레시 토큰이 유효한 경우 액세스 토큰 재발급
    private void checkRefreshTokenAndIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken).ifPresent(
                user -> {
                    try {
                        jwtService.sendAccessToken(response, jwtService.createAccessToken(user.getEmail()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
