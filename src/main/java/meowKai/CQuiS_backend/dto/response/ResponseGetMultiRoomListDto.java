package meowKai.CQuiS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import meowKai.CQuiS_backend.dto.MultiRoomListDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseGetMultiRoomListDto {

    private List<MultiRoomListDto> rooms;
}
