package meowKai.CQuiS_backend.config.security.prod;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.config.security.*;
import meowKai.CQuiS_backend.config.security.local.LocalSecurityConfig;
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

    // WebSocket 요청을 처리하는 필터 클래스 분리
    private class WebSocketJwtFilter extends OncePerRequestFilter {
        private final JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter;

        public WebSocketJwtFilter(JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter) {
            this.jwtAuthenticationProcessingFilter = jwtAuthenticationProcessingFilter;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            // WebSocket 관련 요청은 JWT 필터를 건너뛰기
            if (request.getRequestURI().startsWith("/ws") ||
                    request.getRequestURI().startsWith("/app") ||
                    request.getRequestURI().startsWith("/topic") ||
                    request.getRequestURI().startsWith("/queue")) {
                filterChain.doFilter(request, response);
                return;
            }
            try {
                // 다른 요청은 JWT 필터 적용
                jwtAuthenticationProcessingFilter.doFilter(request, response, filterChain);
            } catch (Exception e) {
                throw new ServletException("JWT Authentication failed", e);
            }
        }
    }

    // HTTP 요청에 대한 보안 필터 체인 구성
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CustomLoginAuthFilter customLoginAuthFilter, JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter) throws Exception {
        JwtAuthenticationProcessingFilter jwtFilter = jwtAuthenticationProcessingFilter();
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
                                "/ws/**",
                                "/app/**",
                                "/topic/**",
                                "/queue/**",
                                "/api/admin/**",
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/username/**",
                                "/api/auth/email/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .permitAll()
                        // 그 외의 요청은 모두 인증 요청
                        .anyRequest().authenticated())
                        .addFilterBefore(
                                new OncePerRequestFilter() {
                                    @Override
                                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                                        if (request.getRequestURI().startsWith("/ws")) {
                                            filterChain.doFilter(request, response);
                                            return;
                                        }
                                        jwtAuthenticationProcessingFilter.doFilter(request, response, filterChain);
                                    }
                                },
                                UsernamePasswordAuthenticationFilter.class
                );

        http
                .addFilterAfter(customLoginAuthFilter(), LogoutFilter.class)
                .addFilterBefore(new WebSocketJwtFilter(jwtFilter), CustomLoginAuthFilter.class);

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
        return new JwtAuthenticationProcessingFilter(jwtService, userRepository);
    }
}