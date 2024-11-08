package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResponseCreateShortQuizFromTextDto {
    private List<String> categoryType;
    private String quizName;
    private String koreanAnswer;
    private String englishAnswer;
}
