package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResposeCheckPasswordDto {

    private Boolean isCorrect;
}
