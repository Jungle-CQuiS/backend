package meowKai.CQuiS_backend.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ChatGptRequestDto {
    private String model; // GPT 모델
    private List<Message> messages; // 메시지

    public ChatGptRequestDto(String model, String systemPrompt, String userPrompt) {
        this.model = model;
        this.messages = List.of(new Message("user", userPrompt), new Message("system", systemPrompt));
    }

    @Data
    @AllArgsConstructor
    static class Message {
        private String role; // system, user
        private String content; // 메시지 내용
    }
}
