package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.ResponseStatus;
import meowKai.CQuiS_backend.domain.RoomUserTeam;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseSubmitTeamDto {

    private Boolean isCorrect;
    private String answer;
    private Integer teamHp;
    private ResponseStatus responseStatus;
    private RoomUserTeam nextOffenseTeam;
}
