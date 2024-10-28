package meowKai.CQuiS_backend.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final JwtService jwtService;

    @Transactional
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        try {
            Optional<String> accessToken = jwtService.extractAccessToken(request);
            if (!jwtService.isTokenValid(accessToken.get())) {
                log.error("유효하지 않은 토큰입니다.");
            }
            jwtService.extractEmail(accessToken.get()).ifPresent(
                    jwtService::removeRefreshToken
            );

        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }
}