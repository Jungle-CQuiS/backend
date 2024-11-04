package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResponseCreateShortQuizFromTextDto {
    private String quizName;
    private String koreanAnswer;
    private String englishAnswer;
}
