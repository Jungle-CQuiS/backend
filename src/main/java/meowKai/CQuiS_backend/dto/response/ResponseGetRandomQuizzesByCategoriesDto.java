package meowKai.CQuiS_backend.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResponseGetRandomQuizzesByCategoriesDto {

    List<Object> randomQuizList;
}
