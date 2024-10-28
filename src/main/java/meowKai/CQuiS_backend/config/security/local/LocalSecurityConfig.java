package meowKai.CQuiS_backend.config.security.local;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@Profile("local")
@RequiredArgsConstructor
public class LocalSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final LocalCorsConfig localCorsConfig;
    private final LogoutService logoutService;

    // HTTP 요청에 대한 보안 필터 체인 구성
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, LogoutService logoutService) throws Exception {
        http
                // CORS 설정
                .cors(cors -> cors.configurationSource(localCorsConfig.corsConfigurationSource()))
                // JWT 사용하기 때문에 세션 상태 STATELESS로 설정
                .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // csrf 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 로그아웃
                .logout(logoutConfig -> logoutConfig
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler(((request, response, authentication) -> SecurityContextHolder.clearContext()))
                )
                // 인증 없이 접근 가능한 요청
                .authorizeHttpRequests(
                        requests -> requests.requestMatchers(
                                "/api/auth/login",
                                "/api/auth/signup",
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
                .addFilterBefore(jwtAuthenticationProcessingFilter(), CustomLoginAuthFilter.class);

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
    public LoginFailureHandler loginFailureHanlder() {
        return new LoginFailureHandler();
    }

    // 로그인 필터
    @Bean
    public CustomLoginAuthFilter customLoginAuthFilter() throws Exception {
        CustomLoginAuthFilter loginFilter = new CustomLoginAuthFilter(objectMapper);
        loginFilter.setAuthenticationManager(authenticationManager());
        loginFilter.setAuthenticationSuccessHandler(loginSuccessJWTProvideHandler());
        loginFilter.setAuthenticationFailureHandler(loginFailureHanlder());
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