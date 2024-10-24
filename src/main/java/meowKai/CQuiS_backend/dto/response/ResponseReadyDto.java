package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResponseReadyDto {
    private Long roomUserId;
    private Boolean isReady;
}
