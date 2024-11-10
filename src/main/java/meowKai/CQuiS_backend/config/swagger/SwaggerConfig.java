package meowKai.CQuiS_backend.config.swagger;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import meowKai.CQuiS_backend.dto.request.RequestLoginDto;
import meowKai.CQuiS_backend.dto.response.ResponseLoginDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        String jwtAccessTokenSchemeName = "JWT AccessToken Authentication";
        // API 요청 헤더에 AccessToken 인증정보 포함
        SecurityRequirement accessTokenRequirement = new SecurityRequirement()
                .addList(jwtAccessTokenSchemeName);
        // AccessToken Security Scheme 설정
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .name(jwtAccessTokenSchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT");

        String jwtRefreshTokenSchemeName = "JWT RefreshToken Authentication";
        SecurityRequirement refreshTokenRequirement = new SecurityRequirement()
                .addList(jwtRefreshTokenSchemeName);
        SecurityScheme refreshTokenScheme = new SecurityScheme()
                .name(jwtRefreshTokenSchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT");

        // Security Scheme 등록
        Components authComponents = new Components()
                .addSecuritySchemes(jwtAccessTokenSchemeName, accessTokenScheme)
                .addSecuritySchemes(jwtRefreshTokenSchemeName, refreshTokenScheme);

        return new OpenAPI()
                .components(authComponents)
                .info(apiInfo())
                .addSecurityItem(accessTokenRequirement)
                .addSecurityItem(refreshTokenRequirement)
                .paths(loginPaths());
    }

    private Info apiInfo() {
        return new Info()
                .title("CQuiS API")
                .description("CQuiS API 명세서입니다.")
                .version("1.0.0");
    }

    private Paths loginPaths() {
        Paths paths = new Paths();

        // 로그인 엔드포인트 정의
        PathItem loginPathItem = new PathItem();

        // POST 요청에 대한 로그인 엔드포인트 설정
        loginPathItem.post(new Operation()
                .tags(List.of("보안"))
                .summary("로그인 with Spring Security")
                .requestBody(new RequestBody()
                        .content(new Content()
                                .addMediaType("application/json", new MediaType()
                                        .schema(new Schema<RequestLoginDto>()
                                                .properties(Map.of(
                                                        "email", new Schema<String>().type("string").example("user@example.com"),
                                                        "password", new Schema<String>().type("string").example("password123")
                                                ))))))
                .responses(new ApiResponses() {{
                    addApiResponse("200", new ApiResponse()
                            .description("로그인 성공")
                            .content(new Content()
                                    .addMediaType("application/json", new MediaType()
                                            .schema(new Schema<ResponseLoginDto>()
                                                    .properties(Map.of(
                                                            "accessToken", new Schema<String>().type("string").example("your_access_token"),
                                                            "refreshToken", new Schema<String>().type("string").example("your_refresh_token")
                                                    ))))));
                    addApiResponse("401", new ApiResponse()
                            .description("인증 실패"));
                }}));

        // 로그인 API URI 설정
        paths.addPathItem("/api/auth/login", loginPathItem);

        return paths;
    }
//    .addSecuritySchemes(jwtRefreshTokenSchemeName, new SecurityScheme()
//                        .name(jwtRefreshTokenSchemeName)
//                        .type(SecurityScheme.Type.HTTP)
//                        .scheme("Bearer")
//                        .bearerFormat("JWT")
//                )
}