package meowKai.CQuiS_backend.config.security.oauth;

import lombok.Getter;
import meowKai.CQuiS_backend.domain.User;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {

    // 인증된 사용자 정보
    private final String name;
    private final String email;

    public SessionUser(User user) {
        this.name = user.getUsername();
        this.email = user.getEmail();
    }
}
