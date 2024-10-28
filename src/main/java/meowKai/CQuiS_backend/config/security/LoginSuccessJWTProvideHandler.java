package meowKai.CQuiS_backend.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessJWTProvideHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess
            (HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        String email = extractEmail(authentication);

        // 로그인 성공 시 JWT 발급
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        userRepository.findByEmail(email).ifPresent(
                user -> jwtService.updateRefreshToken(email, refreshToken)
        );

        log.info("로그인에 성공했습니다. email: {}", email);
        response.getWriter().write("Login Success");
    }

    // Authentication 객체에서 email 추출
    private String extractEmail(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
