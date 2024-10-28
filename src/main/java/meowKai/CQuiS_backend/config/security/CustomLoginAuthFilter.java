package meowKai.CQuiS_backend.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomLoginAuthFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_LOGIN_URL = "/api/auth/login";
    private static final String LOGIN_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";
    private static final String USERNAME_PARAMETER = "email";
    private static final String PASSWORD_PARAMETER = "password";
    private final ObjectMapper objectMapper;

    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_URL, LOGIN_METHOD);

    public CustomLoginAuthFilter(ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        // Content type이 json이 아니거나 null이면 예외 발생
        if (request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {
            throw new AuthenticationServiceException("Authentication Content-Type is not supported: " + request.getContentType());
        }

        // request의 body를 읽어서 username과 password를 추출
        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        // json 형태의 messageBody를 Map으로 변환
        try {
            Map<String, String> loginMap = objectMapper.readValue(messageBody, Map.class);

            // username과 password를 추출
            String username = loginMap.get(USERNAME_PARAMETER);
            String password = loginMap.get(PASSWORD_PARAMETER);

            // username과 password를 기반으로 UsernamePasswordAuthenticationToken을 생성
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

            // ProviderManager의 AuthenticationManager를 통해 인증 요청
            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            throw new IOException("Invalid JSON format", e);
        }
    }
}
