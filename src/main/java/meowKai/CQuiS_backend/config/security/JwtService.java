package meowKai.CQuiS_backend.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public interface JwtService {

    String createAccessToken(String email);
    String createRefreshToken();

    void updateRefreshToken(String email, String refreshToken);
    void removeRefreshToken(String email);

    void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken, UUID uuid) throws IOException;

    // access token, refresh token 전송
    void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken, UUID uuid, String username) throws IOException;

    void sendAccessToken(HttpServletResponse response, String accessToken) throws IOException;

    Optional<String> extractAccessToken(HttpServletRequest request) throws IOException, ServletException;
    Optional<String> extractRefreshToken(HttpServletRequest request) throws IOException, ServletException;
    Optional<String> extractEmail(String accessToken);

    void setAccessTokenHeader(HttpServletResponse response, String accessToken);
    void setRefreshTokenHeader(HttpServletResponse response, String refreshToken);

    boolean isTokenValid(String token);
}
