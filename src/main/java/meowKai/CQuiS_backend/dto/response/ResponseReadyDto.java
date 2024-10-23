package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResponseReadyDto {
    private Boolean isReady;
}
