package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ResponseLoginDto {

    private String accessToken;
    private String refreshToken;
}
