package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RequestSaveSingleGameStatisticsDto {

    private UUID uuid;
//    @Schema(description = "카테고리 별 전체문제/맞춘문제 결과 데이터", example = "1")
//    private List<ResultPerCategory> resultPerCategories;
    private Integer quizCount;
    private Integer correctCount;

//    @Getter
//    @Builder
//    @ToString
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class ResultPerCategory {
//        private Long categoryId;
//        private Integer totalQuizCount;
//        private Integer correctQuizCount;
//    }
}
