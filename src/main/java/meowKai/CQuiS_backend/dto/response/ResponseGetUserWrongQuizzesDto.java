package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseGetUserWrongQuizzesDto {
    private List<Object> wrongQuizzes;
}
