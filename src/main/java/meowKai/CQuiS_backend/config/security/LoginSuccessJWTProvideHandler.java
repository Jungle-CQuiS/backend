package meowKai.CQuiS_backend.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.UUID;

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
        UUID uuid = extractUuid(authentication);
        String username = extractUsername(authentication);

        // 로그인 성공 시 JWT 발급
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken, uuid, username);
        userRepository.findByEmail(email).ifPresent(
                user -> {
                    jwtService.updateRefreshToken(email, refreshToken);
                    user.updateLastAccessed();
                }

        );

        log.info("로그인에 성공했습니다. email: {}", email);
    }

    // Authentication 객체에서 email 추출
    private String extractEmail(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // Authentication 객체에서 uuid 추출
    private UUID extractUuid(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUuid();
    }

    // Authentication 객체에서 username 추출
    private String extractUsername(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getNickname();
    }
}
