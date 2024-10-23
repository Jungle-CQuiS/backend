package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestYieldDto {

    private Long roomUserId;
    private Long yieldUserId;
    private Long roomId;
}
