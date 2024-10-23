package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.application.quiz.KoreanAnalyzer;
import meowKai.CQuiS_backend.application.quiz.SimilarityCalculator;
import meowKai.CQuiS_backend.dto.request.RequestGradeDto;
import meowKai.CQuiS_backend.dto.response.ResponseGradeDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    //private final QuizRepository quizRepository; // TODO: 문제 정보를 가져오기 위한 문제 관련 repository
  
    @Override
    public ResponseGradeDto checkGrade(RequestGradeDto requestGradeDto) {
        log.info("채점 요청 : {}", requestGradeDto);

        // 공백 제거 전처리
        String correctAnswer = "트랜잭션 ".replace(" ", ""); // TODO: 문제 관련 Repository에서 정답 가져오기, 현재 하드코딩 상태
        String userInput = requestGradeDto.getUserInput().replace(" ", "");

        // 영어 음차 표기 여부 확인
        boolean isTrans = KoreanAnalyzer.isTransliteration(userInput);

        double similarity;
        if(!isTrans) {
            similarity = correctAnswer.equals(userInput) ? 1.0 : 0.0;   // 음차 표기가 아니라면 정확하게 일치해야 정답
        } else {
            int length = Math.min(correctAnswer.length(), userInput.length());
            boolean isShort = length <= 2;

            if(isShort) {       // 문자열이 짧으면 가중치를 조절해 Jaro-Winkler 거리를 반영하지 않음
                double[] weights = {0.2125, 0.0, 0.4375, 0.2125, 0.1375};
                similarity = SimilarityCalculator.comprehensiveSimilarity(correctAnswer, userInput, weights);
            } else {
                double[] weights = {0.125, 0.35, 0.35, 0.125, 0.05};
                similarity = SimilarityCalculator.comprehensiveSimilarity(correctAnswer, userInput, weights);
            }
        }

        ResponseGradeDto responseDto = similarity >= 0.9 ? ResponseGradeDto.builder().isCorrect(true).build() : ResponseGradeDto.builder().isCorrect(false).build();
        log.info("채점 완료 : {}", responseDto);
        return responseDto;
    }

}
