package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.domain.ResponseStatus;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseSelectOptionDto {
    private ResponseStatus responseStatus;
    private Long number;
}
