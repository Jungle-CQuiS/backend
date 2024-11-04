package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseSaveSingleGameStatisticsDto {
    private Integer updatedSolvedCount;
    private Integer updatedWrongCount;
    private String updatedCorrectRate;
}