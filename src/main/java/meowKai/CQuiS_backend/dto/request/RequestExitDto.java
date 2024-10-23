package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestExitDto {

    private Long roomUserId;
    private Long roomId;
}
