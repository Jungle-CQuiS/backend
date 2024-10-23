package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponseJoinRoomDto {

    // TODO: authToken(방 인증 토큰) 반환해줘야 함. 나중에 추가하기
    private String authToken;
}