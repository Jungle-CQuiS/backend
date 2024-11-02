package meowKai.CQuiS_backend.config.webclientconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    @Bean
    public WebClient webClient() {

        return WebClient.builder()
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON)
                .build();
    }
}