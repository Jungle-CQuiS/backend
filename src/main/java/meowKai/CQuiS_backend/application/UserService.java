package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;

public interface UserService {
    ResponseGetPersonalUserDataDto getPersonalData(RequestGetPersonalUserDataDto requestDto); // 유저의 개인 정보 받기
    ResponseGetUserStatisticsDto getUserQuizStatistics(RequestGetUserStatisticsDto requestDto); // 유저의 퀴즈 통계 데이터 받기
    ResponseSaveSingleGameStatisticsDto saveStatisticsForSingleGame(RequestSaveSingleGameStatisticsDto requestDto); // 진행한 싱글 게임에 대한 통계 정보 저장
    ResponseGetUserCategoryLevelsDto getUserCategoryLevels(RequestGetUserCategoryLevelsDto requestDto); // 유저의 카테고리 별 레벨 데이터 받기
    ResponseUpdateUserCategoryLevelsDto updateUserCategoryLevelsAfterGame(RequestUpdateUserCategoryLevelsDto requestDto); // 게임이 끝난 후 유저의 카테고리 별 레벨 데이터 업데이트
    ResponseUpdateUserWrongQuizzesDto updateUserWrongQuizzes(RequestUpdateUserWrongQuizzesDto requestDto); // 유저의 틀린 문제 업데이트
    ResponseGetUserWrongQuizzesDto getUserWrongQuizzes(RequestGetUserWrongQuizzesDto requestDto); // 유저의 틀린 문제 받기
    void saveUserQuizLog(RequestSaveUserQuizLogDto requestDto); // 유저의 퀴즈 로그 저장
}
