package meowKai.CQuiS_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestSignUpDto {

    @NotNull
    private String email;
    @NotNull
    private String username;
    @NotNull
    private String password;
}
