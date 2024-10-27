package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestWebSocketJoinRoom {
    private UUID uuid;
    private Long roomId;
}
