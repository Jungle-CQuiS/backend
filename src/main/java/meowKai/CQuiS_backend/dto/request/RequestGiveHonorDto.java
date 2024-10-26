package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestGiveHonorDto {

    private Long honorRoomUserId;
}
