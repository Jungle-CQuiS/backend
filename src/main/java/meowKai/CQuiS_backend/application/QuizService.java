package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.domain.GameRoom;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;

public interface QuizService {
    ResponseGradeDto checkGrade(RequestGradeDto requestDto); // 채점
    ResponseGetCategoriesDto getCategories(); // 카테고리 종류 받기
    ResponseGetShortAnswerQuizzesDto getShortAnswerQuizzesByConditions(RequestGetShortAnswerQuizzesDto requestDto); // 주관식 문제 요청하기
    ResponseGetChoiceAnswerQuizzesDto getChoiceAnswerQuizzesByConditions(RequestGetChoiceAnswerQuizzesDto requestDto); // 객관식 문제 요청하기
    ResponseGetRandomQuizzesByCategoriesDto getRandomQuizzesByCategories(Long roomId); // 카테고리 별로 랜덤 문제 두 문제씩 가져오기
    ResponseSaveSingleGameStatisticsDto saveStatisticsForSingleGame(RequestSaveSingleGameStatisticsDto requestDto); // 진행한 싱글 게임에 대한 통계 정보 저장
    void storeQuizzes(GameRoom gameRoom); // 게임 시작 직전 호출되어 카테고리별로 20개의 문제를 저장해 둠
    void downvoteQuiz(RequestDownvoteDto requestDto); // 문제 평가하기
}
