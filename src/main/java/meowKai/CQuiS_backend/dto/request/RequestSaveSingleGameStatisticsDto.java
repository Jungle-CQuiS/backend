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
    private List<DetailDataDto> detailData;
    private Integer quizCount;
    private Integer correctCount;

    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailDataDto {
        private Long categoryId;
        private Integer totalQuizCount;
        private Integer correctQuizCount;
    }
}
