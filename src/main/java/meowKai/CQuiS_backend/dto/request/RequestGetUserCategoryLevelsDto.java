package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestGetUserCategoryLevelsDto {
    private UUID uuid;
}
