package meowKai.CQuiS_backend.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import meowKai.CQuiS_backend.domain.User;

import java.util.UUID;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ResponseSignUpDto {

    @NotNull
    private String email;
    @NotNull
    private String username;
    @NotNull
    private UUID uuid;

    public static ResponseSignUpDto createDto(User user) {

        ResponseSignUpDto responseDto = ResponseSignUpDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .uuid(user.getUuid())
                .build();
        return responseDto;
    }
}
