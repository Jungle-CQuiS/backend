package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseGetUserStatisticsDto {

    private String singleCorrectRate;
    private Integer multiHonorCount;
}
