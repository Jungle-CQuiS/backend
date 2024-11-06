package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.CategoryType;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestGetUserWrongQuizzesDto {
    private UUID uuid;
    private CategoryType categoryType;
}
