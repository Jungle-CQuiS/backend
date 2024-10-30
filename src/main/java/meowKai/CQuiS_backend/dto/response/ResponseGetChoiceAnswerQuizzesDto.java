package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.dto.GetChoiceAnsQuizDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResponseGetChoiceAnswerQuizzesDto {

    List<GetChoiceAnsQuizDto> quizList;
}
