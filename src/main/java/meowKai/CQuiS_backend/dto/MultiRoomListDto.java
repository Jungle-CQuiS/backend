package meowKai.CQuiS_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MultiRoomListDto {

    private Long gameRoomId;
    private String name;
    private Integer currentUsers;
    private Integer maxUsers;
    private Boolean isLocked;
}
