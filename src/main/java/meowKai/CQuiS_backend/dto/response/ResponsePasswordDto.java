package meowKai.CQuiS_backend.dto.response;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResponsePasswordDto {

    private Boolean isCorrect;
}
