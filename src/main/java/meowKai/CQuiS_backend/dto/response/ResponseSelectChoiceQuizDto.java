package meowKai.CQuiS_backend.dto.response;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString(callSuper = true)
public class ResponseSelectChoiceQuizDto extends ResponseSelectQuizDto {
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;
}
