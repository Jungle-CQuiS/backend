package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ResponseLoginDto {

    private UUID uuid;
    private String username;
    private String accessToken;
    private String refreshToken;
}