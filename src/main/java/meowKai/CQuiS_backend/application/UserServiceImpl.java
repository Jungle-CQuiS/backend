package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.GetChoiceAnsQuizDto;
import meowKai.CQuiS_backend.dto.GetShortAnsQuizDto;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.*;
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
    private final LogDataRepository logDataRepository;
    private final QuizRepository quizRepository;
    private final QuizWrongRepository quizWrongRepository;
    private final UserQuizLogRepository userQuizLogRepository;

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

    // 플레이한 싱글 게임 통계 정보 저장
    @Override
    @Transactional
    public ResponseSaveSingleGameStatisticsDto saveStatisticsForSingleGame(RequestSaveSingleGameStatisticsDto requestDto) {
        log.info("플레이한 싱글 게임 통계 정보 저장 요청 : {}", requestDto);

        User foundUser = userRepository.findByUuid(requestDto.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        UserStatistics foundUserStatistics = userStatisticsRepository.findByUser(foundUser)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 통계 정보입니다."));

        int solvedCount = requestDto.getQuizCount(); // 한 판에서 푼 전체 문제 수
        int correctCount = requestDto.getCorrectCount(); // 한 판에서 맞춘 문제 수
        int wrongCount = solvedCount - correctCount; // 한 판에서 틀린 문제 수

        // 푼 문제 수 업데이트(기존 푼 문제 수 + 게임에서 푼 문제 수)
        foundUserStatistics.updateSolvedCount(solvedCount);
        // 틀린 문제 수 업데이트(기존 틀린 문제 수 + 게임에서 틀린 문제 수)
        foundUserStatistics.updateWrongCount(wrongCount);
        // 정답률 업데이트
        foundUserStatistics.updateCorrectRate();

        ResponseSaveSingleGameStatisticsDto responseDto = ResponseSaveSingleGameStatisticsDto.builder()
                .updatedSolvedCount(foundUserStatistics.getSolvedCount())
                .updatedWrongCount(foundUser.getUserStatistics().getWrongCount())
                .updatedCorrectRate(foundUserStatistics.getCorrectRate())
                .build();

        log.info("플레이한 싱글 게임 통계 정보 저장 응답 : {}", responseDto);
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
                        .categoryType(userCategoryLevel.getCategory().getCategory())
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
                            .categoryType(foundCategoryLevelData.getCategory().getCategory())
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

    // 유저의 틀린 문제 데이터를 업데이트
    @Override
    @Transactional
    public ResponseUpdateUserWrongQuizzesDto updateUserWrongQuizzes(RequestUpdateUserWrongQuizzesDto requestDto) {
        log.info("유저 틀린 문제 업데이트 요청: {}", requestDto);

        User foundUser = userRepository.findByUuid(requestDto.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        List<Long> wrongQuizIds = requestDto.getWrongQuizIds();

        List<ResponseUpdateUserWrongQuizzesDto.UpdateWrongQuizDto> responseList = wrongQuizIds.stream().map(
                wrongQuizId -> {
                    Quiz foundQuiz = quizRepository.findById(wrongQuizId)
                            .orElseThrow(() -> new IllegalArgumentException("해당 퀴즈가 존재하지 않습니다."));
                    quizWrongRepository.save(QuizWrong.builder()
                            .user(foundUser)
                            .quiz(foundQuiz)
                            .build());
                    return ResponseUpdateUserWrongQuizzesDto.UpdateWrongQuizDto.builder()
                            .wrongQuizId(wrongQuizId)
                            .wrongQuizName(foundQuiz.getName())
                            .build();
                }
        ).toList();

        ResponseUpdateUserWrongQuizzesDto responseDto = ResponseUpdateUserWrongQuizzesDto.builder()
                .wrongQuizzes(responseList)
                .build();
        log.info("유저 틀린 문제 업데이트 완료: {}", responseDto);
        return responseDto;
    }

    // 유저의 틀린 문제 데이터를 반환
    @Override
    public ResponseGetUserWrongQuizzesDto getUserWrongQuizzes(RequestGetUserWrongQuizzesDto requestDto) {
        log.info("유저 틀린 문제 정보 반환 요청: {}", requestDto);

        User foundUser = userRepository.findByUuid(requestDto.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 유저가 틀린 문제 리스트
        List<QuizWrong> userWrongQuizzes = quizWrongRepository.findByUser(foundUser);

        List<Object> responseList = userWrongQuizzes.stream()
                .filter(wrongQuiz -> {
                    Quiz foundQuiz = wrongQuiz.getQuiz();
                    return foundQuiz.getCategory().getCategory().equals(requestDto.getCategoryType());
                })
                .map(
                        wrongQuiz -> {
                            Quiz foundQuiz = quizRepository.findById(wrongQuiz.getQuiz().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("해당 퀴즈가 존재하지 않습니다."));
                            QuizType foundQuizType = foundQuiz.getType();
                            switch (foundQuizType) {
                                case SHORT -> {
                                    ShortAnsQuiz foundShortAnsQuiz = foundQuiz.getShortAnsQuiz();
                                    return GetShortAnsQuizDto.createDto(foundShortAnsQuiz);
                                }
                                case CHOICE -> {
                                    ChoiceAnsQuiz foundChoiceAnsQuiz = foundQuiz.getChoiceAnsQuiz();
                                    return GetChoiceAnsQuizDto.createDto(foundChoiceAnsQuiz);
                                }
                                default -> throw new IllegalArgumentException("해당 퀴즈 타입이 존재하지 않습니다: " + foundQuizType);
                            }
                        }
                ).toList();

        ResponseGetUserWrongQuizzesDto responseDto = ResponseGetUserWrongQuizzesDto.createResponseDto(responseList);
        log.info("유저 틀린 문제 정보 반환 완료: {}", responseDto);
        return responseDto;
    }

    // 게임이 끝난 후 유저의 퀴즈 로그 저장
    @Override
    @Transactional
    public void saveUserQuizLog(RequestSaveUserQuizLogDto requestDto) {
        log.info("유저 퀴즈 로그 저장 요청: {}", requestDto);

        User foundUser = userRepository.findByUuid(requestDto.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        LogData foundLogData = logDataRepository.findByUser(foundUser)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 로그 데이터가 존재하지 않습니다."));

        // UserQuizLog와 연관관계 맺는 LogData 엔티티의 UserQuizLog 리스트
        List<UserQuizLog> userQuizLogList = foundLogData.getUserQuizLogs();

        // 새로 추가할 로그 데이터
        List<RequestSaveUserQuizLogDto.QuizLogData> inputQuizLogs = requestDto.getQuizLogDataList();

        inputQuizLogs.forEach(
                quizLogData -> {
                    UserQuizLog createdLog = UserQuizLog.createUserQuizLog(quizLogData, foundLogData);
                    userQuizLogRepository.save(createdLog);
                    userQuizLogList.add(createdLog);
                }
        );
        log.info("유저 퀴즈 로그 저장 완료: {}", inputQuizLogs);
    }
}