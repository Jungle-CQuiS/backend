package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseSubmitTeamDto {

    private Boolean isCorrect;
    private String answer;
    private Integer teamHp;
}
