package meowKai.CQuiS_backend.dto.request;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestCheckPasswordDto {

    private Long roomId;
    private Integer password;
}
