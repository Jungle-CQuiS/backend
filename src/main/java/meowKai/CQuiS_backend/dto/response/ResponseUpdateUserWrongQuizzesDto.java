package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUpdateUserWrongQuizzesDto {

    List<UpdateWrongQuizDto> wrongQuizzes;

    @Builder
    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateWrongQuizDto {
        private Long wrongQuizId;
        private String wrongQuizName;

    }
}
