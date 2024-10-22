package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.dto.MultiRoomListDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResponseGetMultiRoomListDto {

    private List<MultiRoomListDto> rooms;
}
