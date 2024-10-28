package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.UUID;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ResponseSignUpDto {

    private String email;
    private String username;
    private UUID uuid;
}
