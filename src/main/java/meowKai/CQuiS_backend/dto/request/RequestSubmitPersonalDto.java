package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestSubmitPersonalDto {
    private Long roomUserId;
    private String answer;
    private String reason;
    private Long roomId;
}
