package meowKai.CQuiS_backend.config.swagger;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
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
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
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
}