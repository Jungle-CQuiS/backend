package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseGetPersonalUserDataDto {

    private String username;
    private String email;
}
