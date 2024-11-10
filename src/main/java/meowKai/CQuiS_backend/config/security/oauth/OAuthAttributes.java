//package meowKai.CQuiS_backend.config.security.oauth;
//
//import lombok.Builder;
//import lombok.Getter;
//import meowKai.CQuiS_backend.domain.LogData;
//import meowKai.CQuiS_backend.domain.Role;
//import meowKai.CQuiS_backend.domain.User;
//import meowKai.CQuiS_backend.domain.UserStatistics;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@Getter
//public class OAuthAttributes {
//
//    private final Map<String, Object> attributes;
//    private final String nameAttributeKey;
//    private final String name;
//    private final String email;
//
//    @Builder
//    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email) {
//        this.attributes = attributes;
//        this.nameAttributeKey = nameAttributeKey;
//        this.name = name;
//        this.email = email;
//    }
//
//    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
//        return ofGoogle(userNameAttributeName, attributes);
//    }
//
//    // 구글 생성자
//    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
//        return OAuthAttributes.builder()
//                .attributes(attributes)
//                .name(attributes.get("name").toString())
//                .email(attributes.get("email").toString())
//                .nameAttributeKey(userNameAttributeName)
//                .build();
//    }
//
//    // User 엔티티 생성
//    public User toEntity() {
//        return User.createUser(email, name, null);
//    }
//}
