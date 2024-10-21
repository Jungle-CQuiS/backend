package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestSignUpDto {

    // TODO: NOT NULL 추가해주기.
    private String email;
    private String username;
    private String password;
}
