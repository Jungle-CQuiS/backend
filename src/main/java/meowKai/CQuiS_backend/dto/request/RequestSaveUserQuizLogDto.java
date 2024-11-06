package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequestSaveUserQuizLogDto {

    private UUID uuid;
    private List<QuizLogData> quizLogDataList;

    @Builder
    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuizLogData {
        private Long quizId;
        private String quizName;
        private String quizType;
        private Long categoryId;
        private String categoryType;
        private String createdDate; // YYYYMMDD 형식
        private Boolean isCorrect;
    }
}
