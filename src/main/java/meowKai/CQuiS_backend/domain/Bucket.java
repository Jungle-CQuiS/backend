package meowKai.CQuiS_backend.domain;

import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Bucket {

    private Integer maxTokens;
    private Integer tokensPerSeconds;
    private AtomicInteger tokens; // bucket에 있는 현재 token의 수
    private LocalDateTime lastUsedTime; // 마지막으로 token을 사용한 시간

    public Bucket(Integer maxTokens, Integer tokensPerSeconds) {
        this.maxTokens = maxTokens;
        this.tokensPerSeconds = tokensPerSeconds;
        this.tokens = new AtomicInteger(maxTokens);
        this.lastUsedTime = LocalDateTime.now();
    }

    // token 사용 시도
    public boolean tryConsume() {
        refillTokens();
        int previousValue = tokens.get();
        int newValue = tokens.updateAndGet(current
                -> current > 0 ? current - 1 : current);
        return newValue < previousValue;
    }

    private void refillTokens() {
        long milliSecondsDiff = Duration.between(this.lastUsedTime, LocalDateTime.now()).toMillis();
        double secondsDiff = milliSecondsDiff / 1000.0;

        int tokensToAdd = (int) (secondsDiff * tokensPerSeconds);

        tokens.updateAndGet(current
                -> Math.min(current + tokensToAdd, maxTokens));

        this.lastUsedTime = LocalDateTime.now();
    }
}
