package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.RequestGetPersonalUserDataDto;
import meowKai.CQuiS_backend.dto.request.RequestGetUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.request.RequestGetUserStatisticsDto;
import meowKai.CQuiS_backend.dto.request.RequestUpdateUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetPersonalUserDataDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetUserStatisticsDto;
import meowKai.CQuiS_backend.dto.response.ResponseUpdateUserCategoryLevelsDto;

public interface UserService {
    ResponseGetPersonalUserDataDto getPersonalData(RequestGetPersonalUserDataDto requestDto); // 유저의 개인 정보 받기
    ResponseGetUserStatisticsDto getUserQuizStatistics(RequestGetUserStatisticsDto requestDto); // 유저의 퀴즈 통계 데이터 받기
    ResponseGetUserCategoryLevelsDto getUserCategoryLevels(RequestGetUserCategoryLevelsDto requestDto); // 유저의 카테고리 별 레벨 데이터 받기
    ResponseUpdateUserCategoryLevelsDto updateUserCategoryLevelsAfterGame(RequestUpdateUserCategoryLevelsDto requestDto); // 게임이 끝난 후 유저의 카테고리 별 레벨 데이터 업데이트
}
