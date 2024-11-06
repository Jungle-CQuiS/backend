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

    public static ResponseGetUserWrongQuizzesDto createResponseDto(List<Object> wrongQuizzes) {
        return ResponseGetUserWrongQuizzesDto.builder()
                .wrongQuizzes(wrongQuizzes)
                .build();
    }
}
