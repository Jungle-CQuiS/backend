package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseGetMixAnswerQuizzesDto {

    List<Object> quizList;
}
