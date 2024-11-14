package meowKai.CQuiS_backend.dto;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAnswer {

    private Long roomUserId;
    private String username;
    private String answer;
    private String reason;
}
