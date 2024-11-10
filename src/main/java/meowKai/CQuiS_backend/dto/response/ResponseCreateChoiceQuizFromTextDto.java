package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.List;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ResponseCreateChoiceQuizFromTextDto {
    private String categoryType;
    private String quizName;
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;
    private Integer answer;
}
