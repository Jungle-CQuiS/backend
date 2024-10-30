package meowKai.CQuiS_backend.dto.response;

import lombok.*;
import meowKai.CQuiS_backend.dto.GetCategoryDto;

import java.util.List;

@AllArgsConstructor
@ToString
@Builder
@NoArgsConstructor
@Getter
public class ResponseGetCategoriesDto {

    List<GetCategoryDto> categories;
}
