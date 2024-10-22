package meowKai.CQuiS_backend.dto;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MultiRoomListDto {

    private Long gameRoomId;
    private String name;
    private Integer currentUsers;
    private Integer maxUsers;
    private Boolean isLocked;
}
