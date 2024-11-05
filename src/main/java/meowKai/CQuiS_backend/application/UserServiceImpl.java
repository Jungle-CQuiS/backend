package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.domain.UserCategoryLevel;
import meowKai.CQuiS_backend.domain.UserStatistics;
import meowKai.CQuiS_backend.dto.request.RequestGetPersonalUserDataDto;
import meowKai.CQuiS_backend.dto.request.RequestGetUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.request.RequestGetUserStatisticsDto;
import meowKai.CQuiS_backend.dto.request.RequestUpdateUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetPersonalUserDataDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetUserStatisticsDto;
import meowKai.CQuiS_backend.dto.response.ResponseUpdateUserCategoryLevelsDto;
import meowKai.CQuiS_backend.infrastructure.UserCategoryLevelRepository;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import meowKai.CQuiS_backend.infrastructure.UserStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final UserCategoryLevelRepository userCategoryLevelRepository;

    // 유저의 개인 정보(이메일, 유저네임)를 반환
    @Override
    public ResponseGetPersonalUserDataDto getPersonalData(RequestGetPersonalUserDataDto requestDto) {
        log.info("유저 정보 반환 요청: {}", requestDto);

        User foundUser = userRepository.findByUuid(requestDto.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        ResponseGetPersonalUserDataDto responseDto = ResponseGetPersonalUserDataDto.builder()
                .username(foundUser.getUsername())
                .email(foundUser.getEmail())
                .build();

        log.info("유저 정보 반환 완료: {}", responseDto);
        return responseDto;
    }

    // 유저의 퀴즈 관련 통계 데이터를 반환
    @Override
    public ResponseGetUserStatisticsDto getUserQuizStatistics(RequestGetUserStatisticsDto requestDto) {
        log.info("유저 퀴즈 통계 정보 반환 요청: {}", requestDto);

        User foundUser = userRepository.findByUuid(requestDto.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        UserStatistics foundUserStatistics = userStatisticsRepository.findByUser(foundUser)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 통계 데이터가 존재하지 않습니다."));

        ResponseGetUserStatisticsDto responseDto = ResponseGetUserStatisticsDto.builder()
                .singleCorrectRate(foundUserStatistics.getCorrectRate())
                .multiHonorCount(foundUserStatistics.getHonorCount())
                .build();

        log.info("유저 퀴즈 통계 정보 반환 완료: {}", responseDto);
        return responseDto;
    }

    // 유저의 카테고리 별 레벨 데이터를 반환
    @Override
    public ResponseGetUserCategoryLevelsDto getUserCategoryLevels(RequestGetUserCategoryLevelsDto requestDto) {
        log.info("유저 카테고리 레벨 정보 반환 요청: {}", requestDto);

        User foundUser = userRepository.findByUuid(requestDto.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        List<UserCategoryLevel> userCategoryLevels = userCategoryLevelRepository.findByUser(foundUser);

        // 카테고리 별 레벨 데이터를 DTO로 변환
        List<ResponseGetUserCategoryLevelsDto.CategoryLevelData> categoryLevelDataList = userCategoryLevels.stream()
                .map(userCategoryLevel -> ResponseGetUserCategoryLevelsDto.CategoryLevelData.builder()
                        .categoryName(userCategoryLevel.getCategory().getCategory())
                        .categoryLevel(userCategoryLevel.getLevel())
                        .build())
                .toList();

        ResponseGetUserCategoryLevelsDto responseDto = ResponseGetUserCategoryLevelsDto.builder()
                .categoryLevels(categoryLevelDataList)
                .build();

        log.info("유저 카테고리 레벨 정보 반환 완료: {}", responseDto);
        return responseDto;
    }

    // 게임이 끝난 후 유저의 카테고리 별 레벨 데이터를 업데이트
    @Override
    @Transactional
    public ResponseUpdateUserCategoryLevelsDto updateUserCategoryLevelsAfterGame(RequestUpdateUserCategoryLevelsDto requestDto) {
        log.info("게임 종료 후 유저 카테고리 레벨 업데이트 요청: {}", requestDto);

        User foundUser = userRepository.findByUuid(requestDto.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 게임 결과에 따른 카테고리 별 맞춘 문제 수 리스트
        List<RequestUpdateUserCategoryLevelsDto.GameResultDataDto> gameResult = requestDto.getDetailData();
        List<ResponseUpdateUserCategoryLevelsDto.CategoryLevelData> categoryLevels = gameResult.stream().map(
                gameData -> {
                    UserCategoryLevel foundCategoryLevelData = userCategoryLevelRepository.findByUserAndCategoryId(foundUser, gameData.getCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("유저의 해당 카테고리에 대한 레벨 데이터가 존재하지 않습니다."));

                    foundCategoryLevelData.updateCorrectCount(gameData.getCorrectQuizCount()); // 맞은 문제 수 & 카테고리 레벨 업데이트
                    return ResponseUpdateUserCategoryLevelsDto.CategoryLevelData.builder()
                            .categoryName(foundCategoryLevelData.getCategory().getCategory())
                            .categoryLevel(foundCategoryLevelData.getLevel())
                            .build();
                }
        ).toList();

        ResponseUpdateUserCategoryLevelsDto responseDto = ResponseUpdateUserCategoryLevelsDto.builder()
                .categoryLevels(categoryLevels)
                .build();

        log.info("게임 종료 후 유저 카테고리 레벨 업데이트 완료: {}", responseDto);
        return responseDto;
    }
}
