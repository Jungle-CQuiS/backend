package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.RequestGetChoiceAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestGetShortAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestGradeDto;
import meowKai.CQuiS_backend.dto.request.RequestSaveSingleGameStatisticsDto;
import meowKai.CQuiS_backend.dto.response.*;

public interface QuizService {
    ResponseGradeDto checkGrade(RequestGradeDto requestDto); // 채점
    ResponseGetCategoriesDto getCategories(); // 카테고리 종류 받기
    ResponseGetShortAnswerQuizzesDto getShortAnswerQuizzesByConditions(RequestGetShortAnswerQuizzesDto requestDto); // 주관식 문제 요청하기
    ResponseGetChoiceAnswerQuizzesDto getChoiceAnswerQuizzesByConditions(RequestGetChoiceAnswerQuizzesDto requestDto); // 객관식 문제 요청하기
    ResponseGetRandomQuizzesByCategoriesDto getRandomQuizzesByCategories(); // 카테고리 별로 랜덤 문제 두 문제씩 가져오기
    ResponseSaveSingleGameStatisticsDto saveStatisticsForSingleGame(RequestSaveSingleGameStatisticsDto requestDto); // 진행한 싱글 게임에 대한 통계 정보 저장
}
