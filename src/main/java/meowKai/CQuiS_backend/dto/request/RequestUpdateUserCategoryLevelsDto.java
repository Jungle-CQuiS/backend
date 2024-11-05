package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RequestUpdateUserCategoryLevelsDto {
    private UUID uuid;
    private List<GameResultDataDto> detailData;

    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class GameResultDataDto {
        private Long categoryId;
        private Integer correctQuizCount;
    }
}