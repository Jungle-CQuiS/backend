package meowKai.CQuiS_backend.config.openai;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiSecretKey;

    private final String shortQuizJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "quizzes": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "categoryType": {
                                    "type": "array",
                                    "items": { "type": "string" }
                                },
                                "quizName": { "type": "string" },
                                "koreanAnswer": { "type": "string" },
                                "englishAnswer": { "type": "string" }
                            },
                            "required": ["categoryType", "quizName", "koreanAnswer", "englishAnswer"],
                            "additionalProperties": false
                        }
                    }
                },
                "required": ["quizzes"],
                "additionalProperties": false
            }
            """;

    private final String choiceQuizJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "quizzes": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "categoryType": {
                                    "type": "array",
                                    "items": { "type": "string" }
                                },
                                "quizName": { "type": "string" },
                                "choice1": { "type": "string" },
                                "choice2": { "type": "string" },
                                "choice3": { "type": "string" },
                                "choice4": { "type": "string" },
                                "answer": { "type": "integer" }
                            },
                            "required": ["categoryType", "quizName", "choice1", "choice2", "choice3", "choice4", "answer"],
                            "additionalProperties": false
                        }
                    }
                },
                "required": ["quizzes"],
                "additionalProperties": false
            }
            """;

    public static final String DEFAULT_CHAT_URL = "https://api.openai.com";
}
