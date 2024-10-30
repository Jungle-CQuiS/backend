package meowKai.CQuiS_backend.config.security.prod;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("prod")
public class ProdCorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedMethods(Arrays.asList("GET", "POST"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedOriginPatterns(
                // TODO: 배포 시 swagger 삭제하기!
                List.of(
                        "http://localhost:3000",
                        "https://localhost:3000",
                        "ws://localhost:8080/ws", // TODO: 임시, 나중에 삭제하기
                        "https://cquis.net",
                        "https://dev.cquis.net",
                        "http://cquis.net",
                        "http://dev.cquis.net",
                        "/ws/**", // 웹 소켓
                        "ws/**", // 웹 소켓
                        "ws://**", // 웹 소켓
                        "wss://**", // 웹 소켓
                        "/swagger-ui/**"
                )
        );
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}