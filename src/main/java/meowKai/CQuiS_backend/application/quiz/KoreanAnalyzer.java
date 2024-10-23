package meowKai.CQuiS_backend.application.quiz;

import java.util.Arrays;
import java.util.List;

public class KoreanAnalyzer {
    // 상수 선언
    private static final String CHOSUNG = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
    private static final String JUNGSUNG = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
    private static final String JONGSUNG = "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";

    private static final List<String> COMMON_PATTERNS = Arrays.asList(
            "션", "터", "티", "디", "브", "크", "지", "매니",
            "링", "팅", "킹", "밍", "닝", "슨", "쳐", "플",
            "프", "스", "트", "드", "그", "젝", "워", "셔",
            "즈", "츠", "버", "퓨", "뷰", "큐", "듀", "튜",
            "씨", "폰", "컨", "던", "텐", "맨", "잉", "벤",
            "익", "릭", "닉", "픽", "믹", "윙", "빙", "밴",
            "케", "테"
    );

    // 한글 문자열을 초성, 중성, 종성으로 분해
    public static String decompose(String text) {
        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if ('가' <= ch && ch <= '힣') {
                int charCode = ch - '가';

                int cho = charCode / (21 * 28);
                int jung = (charCode % (21 * 28)) / 28;
                jung = (jung == 5) ? 1 : jung; // ㅐ와 ㅔ를 동일하게 처리
                int jong = charCode % 28;

                result.append(CHOSUNG.charAt(cho));
                result.append(JUNGSUNG.charAt(jung));
                if (jong > 0) {
                    result.append(JONGSUNG.charAt(jong - 1));
                }
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    // 문자열이 음차 표기인지 아닌지 판단
    public static boolean isTransliteration(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        // 1. 패턴 매칭 검사
        for (String pattern : COMMON_PATTERNS) {
            if (word.contains(pattern)) {
                return true;
            }
        }

        // 2. 된소리로 시작하는지 검사 - 된소리로 시작하면 음차 표기일 가능성이 높음
        String decomposed = decompose(String.valueOf(word.charAt(0)));
        if (decomposed.startsWith("ㄲ") || decomposed.startsWith("ㄸ") ||
                decomposed.startsWith("ㅃ") || decomposed.startsWith("ㅆ") ||
                decomposed.startsWith("ㅉ")) {
            return true;
        }

        // 3. 모음 조합 검사
        String fullDecomposed = decompose(word);
        for (int i = 0; i < fullDecomposed.length() - 1; i++) {
            for (String vowelComb : VOWEL_COMBINATIONS) {
                if (i + vowelComb.length() <= fullDecomposed.length()) {    // 길이 체크
                    String substr = fullDecomposed.substring(i, i + vowelComb.length());
                    if (substr.equals(vowelComb)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static final List<String> VOWEL_COMBINATIONS = Arrays.asList(
            "ㅐㅣ", "ㅔㅣ", "ㅏㅣ", "ㅓㅣ", "ㅗㅣ", "ㅜㅣ"
    );
}
