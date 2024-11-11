package meowKai.CQuiS_backend.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserChoiceAnswerCollection {
    private Integer choice;
    private List<String> reasonList;
    private List<Integer> indexList;
}
