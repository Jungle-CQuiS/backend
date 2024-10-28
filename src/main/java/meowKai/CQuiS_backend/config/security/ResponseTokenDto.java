package meowKai.CQuiS_backend.config.security;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class ResponseTokenDto {

    private String accessToken;
    private String refreshToken;
}