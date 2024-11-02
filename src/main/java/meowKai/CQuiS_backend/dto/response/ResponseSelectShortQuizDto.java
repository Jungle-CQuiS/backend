package meowKai.CQuiS_backend.dto.response;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString(callSuper = true)
public class ResponseSelectShortQuizDto extends ResponseSelectQuizDto {
}
