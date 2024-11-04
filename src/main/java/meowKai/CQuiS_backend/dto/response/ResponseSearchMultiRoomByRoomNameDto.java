package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.dto.MultiRoomDto;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResponseSearchMultiRoomByRoomNameDto {
    List<MultiRoomDto> multiRooms;
    Integer nextPageNumber;
}
