package meowKai.CQuiS_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestSignUpDto {

    // TODO: NOT NULL 추가해주기.
    private String email;
    private String username;
    private String password;
}
