package meowKai.CQuiS_backend.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum QuizType {
    SHORT("주관식"),
    CHOICE("객관식"),
    MIX("혼합");

    @JsonValue
    private final String koreanFieldName;

    @JsonCreator
    public static QuizType from(String koreanFieldName) {
        for (QuizType type : QuizType.values()) {
            if (type.getKoreanFieldName().equals(koreanFieldName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid quiz type: " + koreanFieldName);
    }

    QuizType(String koreanFieldName) {
        this.koreanFieldName = koreanFieldName;
    }
}
