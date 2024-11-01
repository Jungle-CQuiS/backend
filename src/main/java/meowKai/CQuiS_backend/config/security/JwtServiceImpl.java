package meowKai.CQuiS_backend.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.dto.response.ResponseLoginDto;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService{

    // jwt.yml에서 값 가져옴
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private Long accessExpirationInSeconds;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpirationInSeconds;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USERNAME_CLAIM = "username";
    private static final String BEARER = "Bearer ";

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // access token 생성
    @Override
    public String createAccessToken(String email) {
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(System.currentTimeMillis() + accessExpirationInSeconds * 1000))
                .withClaim(USERNAME_CLAIM, email)
                .sign(Algorithm.HMAC512(secret));
    }

    // refresh token 생성
    @Override
    public String createRefreshToken() {
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshExpirationInSeconds * 1000))
                .sign(Algorithm.HMAC512(secret));
    }

    // refresh token 갱신
    @Override
    public void updateRefreshToken(String email, String refreshToken) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        user -> user.updateRefreshToken(refreshToken),
                        () -> new Exception("회원 조회 실패")
                );
    }

    // refresh token 삭제
    @Override
    public void removeRefreshToken(String email) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        User::removeRefreshToken,
                        () -> new Exception("회원 조회 실패")
                );
    }

    // access token, refresh token 전송
    @Override
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken, UUID uuid) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);

        ResponseLoginDto responseDto = ResponseLoginDto
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .uuid(uuid)
                .build();
//        Map<String, String> tokenMap = Map.of(ACCESS_TOKEN_SUBJECT, accessToken, REFRESH_TOKEN_SUBJECT, refreshToken);
        String responseData = objectMapper.writeValueAsString(responseDto);
        response.getWriter().write(responseData);
    }

    // 기존의 반환 + 닉네임 전송
    @Override
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken, UUID uuid, String username) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);

        ResponseLoginDto responseDto = ResponseLoginDto
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .uuid(uuid)
                .username(username)
                .build();
//        Map<String, String> tokenMap = Map.of(ACCESS_TOKEN_SUBJECT, accessToken, REFRESH_TOKEN_SUBJECT, refreshToken);
        String responseData = objectMapper.writeValueAsString(responseDto);
        response.getWriter().write(responseData);
    }

    // access token 전송
    @Override
    public void sendAccessToken(HttpServletResponse response, String accessToken) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);

        Map<String, String> tokenMap = Map.of(ACCESS_TOKEN_SUBJECT, accessToken);
        String token = objectMapper.writeValueAsString(tokenMap);
        response.getWriter().write(token);
    }

    // header로부터 access token 추출
    @Override
    public Optional<String> extractAccessToken(HttpServletRequest request) throws IOException, ServletException {
        return Optional.ofNullable(
                        request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    // header로부터 refresh token 추출
    @Override
    public Optional<String> extractRefreshToken(HttpServletRequest request) throws IOException, ServletException {
        return Optional.ofNullable(
                        request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    @Override
    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(
                    JWT.require(Algorithm.HMAC512(secret)).build()
                            .verify(accessToken).getClaim(USERNAME_CLAIM)
                            .asString()
            );
        } catch (Exception e) {
            log.error("토큰에서 이메일 추출 실패");
        }
        return Optional.empty();
    }

    // header에 access token 설정
    @Override
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    // header에 refresh token 설정
    @Override
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    // 토큰의 유효성 검증
    @Override
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secret)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다.");
        }
        return false;
    }
}
