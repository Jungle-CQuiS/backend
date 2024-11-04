package meowKai.CQuiS_backend.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CategoryType {
    ALGORITHM("알고리즘"),
    DATABASE("데이터베이스"),
    DATASTRUCTURE("자료구조"),
    NETWORK("네트워크"),
    OS("OS");

    @JsonValue
    private final String koreanFieldName;

    @JsonCreator
    public static CategoryType from(String koreanFieldName) {
        for (CategoryType type : CategoryType.values()) {
            if(type.getKoreanFieldName().equals(koreanFieldName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid category type: " + koreanFieldName);
    }

    CategoryType(String koreanFieldName) {
        this.koreanFieldName = koreanFieldName;
    }
}
