package meowKai.CQuiS_backend.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RequestCreateMultiRoomDto {

    private String name;
    private UUID uuid;
    private Integer password;
    private Integer maxUser;
}
