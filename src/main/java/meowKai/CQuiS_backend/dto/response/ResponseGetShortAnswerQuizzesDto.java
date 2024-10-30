package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.dto.GetShortAnsQuizDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResponseGetShortAnswerQuizzesDto {

    List<GetShortAnsQuizDto> quizList;
}