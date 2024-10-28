package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RequestLoginDto {

    private String email;
    private String password;
}
