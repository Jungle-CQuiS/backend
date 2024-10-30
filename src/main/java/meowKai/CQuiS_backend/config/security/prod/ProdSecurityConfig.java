package meowKai.CQuiS_backend.config.security.prod;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.config.security.*;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@Profile("prod")
@RequiredArgsConstructor
public class ProdSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ProdCorsConfig prodCorsConfig;

    // HTTP 요청에 대한 보안 필터 체인 구성
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CustomLoginAuthFilter customLoginAuthFilter, JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter) throws Exception {
        http
                // CORS 설정
                .cors(cors -> cors.configurationSource(prodCorsConfig.corsConfigurationSource()))
                // JWT 사용하기 때문에 세션 상태 STATELESS로 설정
                .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // TODO: https 프로토콜 요청만 허용하도록 나중에 바꿔야함.
                // https 프로토콜 요청만 허용
//                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
                // csrf 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 로그 아웃 시 세션 무효화 및 로그인 페이지로 이동
                .logout(logout -> logout.invalidateHttpSession(true)
                )
                // 인증 없이 접근 가능한 요청
                // TODO: 개발 끝나면 swagger-ui 지우기
                .authorizeHttpRequests(requests -> requests.requestMatchers(
//                                "/", // 메인 페이지
//                                "/index.html", // 메인 페이지
//                                "/*.html", // HTML 파일들
//                                "/*.js", // JS 파일들
//                                "/*.css", // CSS 파일들
//                                "/*.ico", // favicon
//                                "/assets/**", // 정적 리소스들
//                                "/static/**", // 정적 리소스들
//                                "/images/**", // 이미지 파일들
                                "ws/**", // 웹 소켓 기본
                                "/ws/**", // 웹 소켓 하위 경로
                                "/topic/**", // 구독
                                "/queue/**", // 개인 메시지
                                "/app/**", // 메시지 발행
                                "/user/**", // 사용자별 메시지
                                "/api/admin/**",
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/admin/health-check",
                                "/api/auth/username/**",
                                "/api/auth/email/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .permitAll()
                        // 그 외의 요청은 모두 인증 요청
                        .anyRequest().authenticated());

        http
                .addFilterAfter(customLoginAuthFilter(), LogoutFilter.class)
                .addFilterBefore(
                        new OncePerRequestFilter() {
                            @Override
                            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                                // 웹소켓 관련 모든 요청 체크
                                if (isWebSocketRequest(request)) {
                                    filterChain.doFilter(request, response);
                                    return;
                                }
                                jwtAuthenticationProcessingFilter.doFilter(request, response, filterChain);
                            }

                            private boolean isWebSocketRequest(HttpServletRequest request) {
                                // Upgrade 헤더와 Connection 헤더를 확인하여 WebSocket 요청을 식별
                                String upgradeHeader = request.getHeader("Upgrade");
                                String connectionHeader = request.getHeader("Connection");

                                String path = request.getRequestURI();

                                return path.startsWith("/ws") || //
                                        path.startsWith("/topic") ||
                                        path.startsWith("/app") ||
                                        path.startsWith("/queue") ||
                                        path.startsWith("/user") ||
                                        "websocket".equalsIgnoreCase(upgradeHeader) ||
                                        "Upgrade".equalsIgnoreCase(connectionHeader);
                            }
                        },
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    // 인증 관리자 관련 설정
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() throws Exception {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    // 인증 관리자 관련 설정
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        DaoAuthenticationProvider daoAuthProvider = daoAuthenticationProvider();
        daoAuthProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthProvider);
    }

    // 비밀번호 암호화
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 로그인 성공 시 핸들러
    @Bean
    public LoginSuccessJWTProvideHandler loginSuccessJWTProvideHandler() {
        return new LoginSuccessJWTProvideHandler(jwtService, userRepository);
    }

    // 로그인 실패 시 핸들러
    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    // 로그인 필터
    @Bean
    public CustomLoginAuthFilter customLoginAuthFilter() throws Exception {
        CustomLoginAuthFilter loginFilter = new CustomLoginAuthFilter(objectMapper);
        loginFilter.setAuthenticationManager(authenticationManager());
        loginFilter.setAuthenticationSuccessHandler(loginSuccessJWTProvideHandler());
        loginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return loginFilter;
    }

    // jwt 인증 필터
    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() throws Exception {
        JwtAuthenticationProcessingFilter jsonUsernamePasswordLoginFilter =
                new JwtAuthenticationProcessingFilter(jwtService, userRepository);

        return jsonUsernamePasswordLoginFilter;
    }
}