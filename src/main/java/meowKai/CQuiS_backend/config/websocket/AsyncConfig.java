package meowKai.CQuiS_backend.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

/**
 * 비동기 작업을 처리하기 위한 스케줄러
 */
@Configuration
public class AsyncConfig {

    @Bean
    public SimpleAsyncTaskScheduler simpleAsyncTaskScheduler() {
        SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setThreadNamePrefix("AsyncTask-");
        return scheduler;
    }
}
