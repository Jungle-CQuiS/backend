package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.application.quiz.KoreanAnalyzer;
import meowKai.CQuiS_backend.application.quiz.SimilarityCalculator;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.GetCategoryDto;
import meowKai.CQuiS_backend.dto.GetChoiceAnsQuizDto;
import meowKai.CQuiS_backend.dto.GetShortAnsQuizDto;
import meowKai.CQuiS_backend.dto.request.RequestGetChoiceAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestGetShortAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestGradeDto;
import meowKai.CQuiS_backend.dto.request.RequestSaveSingleGameStatisticsDto;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseGradeDto checkGrade(RequestGradeDto requestDto) {
        log.info("채점 요청 : {}", requestDto);

        Quiz foundQuiz = quizRepository.findById(requestDto.getQuizId()).orElseThrow(
                () -> new NoSuchElementException("채점 요청 - 존재하지 않는 퀴즈입니다.")
        );

        String userInput = requestDto.getUserInput().replace(" ", ""); // 채점할 사용자의 입력

        boolean isChoice = foundQuiz.getType().equals(QuizType.CHOICE); // 퀴즈가 객관식인지
        boolean isEnglish = userInput.matches("[a-zA-Z]+"); // 영어로만 이루어져 있는지
        boolean isTrans = KoreanAnalyzer.isTransliteration(userInput); // 음차 표기인지

        String correctAnswer = getCorrectAnswer(foundQuiz, isEnglish);

        double similarity;
        if(isChoice || isEnglish || !isTrans) {
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

        ResponseGradeDto responseDto = similarity >= 0.9 ?
                ResponseGradeDto.builder()
                        .isCorrect(true)
                        .build()
                : ResponseGradeDto.builder()
                        .isCorrect(false)
                        .build();

        log.info("채점 완료 : {}", responseDto);
        return responseDto;
    }

    // 문제의 타입과 유저의 입력에 따라 다른 정답을 가져옴
    private String getCorrectAnswer(Quiz quiz, boolean isEnglish) {
        if(quiz.getType() == QuizType.CHOICE) {
            return quiz.getChoiceAnsQuiz().getAnswer().toString();
        }

        return isEnglish ?
                quiz.getShortAnsQuiz().getEnglishAnswer().replace(" ", "").toLowerCase() :
                quiz.getShortAnsQuiz().getKoreanAnswer().replace(" ", "");
    }

    // 카테고리 정보 가져오기
    @Override
    public ResponseGetCategoriesDto getCategories() {
        log.info("카테고리 정보 요청");
        List<GetCategoryDto> categories = categoryRepository.findAll().stream()
                .map(category -> GetCategoryDto.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getCategory())
                        .build())
                .toList();

        ResponseGetCategoriesDto responseDto = ResponseGetCategoriesDto.builder()
                .categories(categories)
                .build();

        log.info("카테고리 정보 응답 : {}", responseDto);
        return responseDto;
    }

    // 주관식 문제를 주어진 카테고리 내에서 랜덤하게 유저가 입력한 갯수만큼 랜덤하게 가져오기
    @Override
    public ResponseGetShortAnswerQuizzesDto getShortAnswerQuizzesByConditions(RequestGetShortAnswerQuizzesDto requestDto) {
        log.info("주관식 문제 요청 : {}", requestDto);

        // 카테고리 별로 할당할 문제 갯수를 저장하는 map
        Map<Long, Integer> quizzesPerCategory = new HashMap<>();
        List<Long> categoryIds = requestDto.getCategoryIds();
        int categoryIdsCount = categoryIds.size();
        int quizCount = requestDto.getQuizCount();

        for (Long categoryId : categoryIds) {
            quizzesPerCategory.put(categoryId, 0); // {카테고리 아이디: 문제 갯수}
        }

        // 카테고리 갯수와 문제 갯수가 같으면 카테고리 별로 1문제 씩 가져옴
        if (categoryIdsCount == quizCount) {
            quizzesPerCategory.keySet().forEach(id -> quizzesPerCategory.put(id, 1));
        }
        // 카테고리 갯수 < 퀴즈 갯수면 카테고리 별로 n개 뽑아서 합이 퀴즈 갯수가 되도록
        else if (categoryIdsCount < quizCount) {
            int baseCount = quizCount / categoryIdsCount;
            int remainCount = quizCount % categoryIdsCount;

            for (int i = 0; i < categoryIdsCount; i++) {
                Long categoryId = requestDto.getCategoryIds().get(i);
                quizzesPerCategory.put(categoryId, baseCount + (i < remainCount ? 1: 0));
            }
        }
        // 카테고리 갯수 > 퀴즈 갯수면 퀴즈 0개가 할당되는 카테고리가 최소화 되도록
        else {
            for (int i = 0; i < quizCount; i++) {
                Long categoryId = requestDto.getCategoryIds().get(i);
                quizzesPerCategory.put(categoryId, 1);
            }
        }

        ResponseGetShortAnswerQuizzesDto responseDto = ResponseGetShortAnswerQuizzesDto.builder().build();
        quizzesPerCategory.forEach((key, value) -> {
            // 카테고리 별로 가져와야 할 문제 갯수
            long categoryId = key;
            int count = value;

            // 조건에 해당하는 문제 모두 불러오기
            List<Quiz> quizzesFitConditions = quizRepository.findAllByCategoryIdAndType(categoryId, QuizType.SHORT);

            // 카테고리에 해당하는 문제 갯수보다 요청한 문제의 수가 더 많으면 카테고리의 모든 문제를 가져옴
            if (count >= quizzesFitConditions.size()) {
                quizzesFitConditions.forEach(quiz -> {
                    responseDto.getQuizList().add(
                            GetShortAnsQuizDto.builder()
                                    .categoryId(quiz.getCategory().getId())
                                    .quizId(quiz.getId())
                                    .categoryType(quiz.getCategory().getCategory())
                                    .name(quiz.getName())
                                    .englishAnswer(quiz.getShortAnsQuiz().getEnglishAnswer())
                                    .koreanAnswer(quiz.getShortAnsQuiz().getKoreanAnswer())
                                    .build()
                    );
                });
            }
            else {
                // 문제를 랜덤하게 섞어서 count만큼 가져옴
                Collections.shuffle(quizzesFitConditions);

                List<Quiz> randomQuizzes = quizzesFitConditions.stream()
                        .limit(count)
                        .toList();

                for (Quiz quiz : randomQuizzes) {
                    responseDto.getQuizList().add(
                            GetShortAnsQuizDto.builder()
                                    .categoryId(quiz.getCategory().getId())
                                    .quizId(quiz.getId())
                                    .categoryType(quiz.getCategory().getCategory())
                                    .name(quiz.getName())
                                    .englishAnswer(quiz.getShortAnsQuiz().getEnglishAnswer())
                                    .koreanAnswer(quiz.getShortAnsQuiz().getKoreanAnswer())
                                    .build()
                    );
                }
            }
        });
        log.info("주관식 문제 응답 : {}", responseDto);
        return responseDto;
    }

    // 객관식 문제를 주어진 카테고리 내에서 유저가 입력한 갯수만큼 랜덤하게 가져오기
    @Override
    public ResponseGetChoiceAnswerQuizzesDto getChoiceAnswerQuizzesByConditions(RequestGetChoiceAnswerQuizzesDto requestDto) {
        log.info("객관식 문제 요청 : {}", requestDto);

        // 카테고리 별로 할당할 문제 갯수를 저장하는 map
        Map<Long, Integer> quizzesPerCategory = new HashMap<>();
        List<Long> categoryIds = requestDto.getCategoryIds();
        int categoryIdsCount = categoryIds.size();
        int quizCount = requestDto.getQuizCount();

        for (Long categoryId : categoryIds) {
            quizzesPerCategory.put(categoryId, 0); // {카테고리 아이디: 문제 갯수}
        }

        // 카테고리 갯수와 문제 갯수가 같으면 카테고리 별로 1문제 씩 가져옴
        if (categoryIdsCount == quizCount) {
            quizzesPerCategory.keySet().forEach(id -> quizzesPerCategory.put(id, 1));
        }
        // 카테고리 갯수 < 퀴즈 갯수면 카테고리 별로 n개 뽑아서 합이 퀴즈 갯수가 되도록
        else if (categoryIdsCount < quizCount) {
            int baseCount = quizCount / categoryIdsCount;
            int remainCount = quizCount % categoryIdsCount;

            for (int i = 0; i < categoryIdsCount; i++) {
                Long categoryId = requestDto.getCategoryIds().get(i);
                quizzesPerCategory.put(categoryId, baseCount + (i < remainCount ? 1 : 0));
            }
        }
        // 카테고리 갯수 > 퀴즈 갯수면 퀴즈 0개가 할당되는 카테고리가 최소화 되도록
        else {
            for (int i = 0; i < quizCount; i++) {
                Long categoryId = requestDto.getCategoryIds().get(i);
                quizzesPerCategory.put(categoryId, 1);
            }
        }

        ResponseGetChoiceAnswerQuizzesDto responseDto = ResponseGetChoiceAnswerQuizzesDto.builder().build();

        quizzesPerCategory.forEach((key, value) -> {

            // 카테고리 별로 가져와야 할 문제 갯수
            long categoryId = key;
            int count = value;

            // 조건에 해당하는 문제 모두 불러오기
            List<Quiz> quizzesFitConditions = quizRepository.findAllByCategoryIdAndType(categoryId, QuizType.CHOICE);

            // 카테고리에 해당하는 문제 갯수보다 요청한 문제의 수가 더 많으면 카테고리의 모든 문제를 가져옴
            if (count >= quizzesFitConditions.size()) {
                quizzesFitConditions.forEach(quiz -> {
                    responseDto.getQuizList().add(
                            GetChoiceAnsQuizDto.builder()
                                    .categoryId(quiz.getCategory().getId())
                                    .quizId(quiz.getId())
                                    .categoryType(quiz.getCategory().getCategory())
                                    .name(quiz.getName())
                                    .choice1(quiz.getChoiceAnsQuiz().getChoice1())
                                    .choice2(quiz.getChoiceAnsQuiz().getChoice2())
                                    .choice3(quiz.getChoiceAnsQuiz().getChoice3())
                                    .choice4(quiz.getChoiceAnsQuiz().getChoice4())
                                    .answer(quiz.getChoiceAnsQuiz().getAnswer())
                                    .build()
                    );
                });
            } else {
                // 문제를 랜덤하게 섞어서 count만큼 가져옴
                Collections.shuffle(quizzesFitConditions);

                List<Quiz> randomQuizzes = quizzesFitConditions.stream()
                        .limit(count)
                        .toList();

                for (Quiz quiz : randomQuizzes) {
                    responseDto.getQuizList().add(
                            GetChoiceAnsQuizDto.builder()
                                    .categoryId(quiz.getCategory().getId())
                                    .quizId(quiz.getId())
                                    .categoryType(quiz.getCategory().getCategory())
                                    .name(quiz.getName())
                                    .choice1(quiz.getChoiceAnsQuiz().getChoice1())
                                    .choice2(quiz.getChoiceAnsQuiz().getChoice2())
                                    .choice3(quiz.getChoiceAnsQuiz().getChoice3())
                                    .choice4(quiz.getChoiceAnsQuiz().getChoice4())
                                    .answer(quiz.getChoiceAnsQuiz().getAnswer())
                                    .build()
                    );
                }
            }
        });
        log.info("객관식 문제 요청 응답 : {}", responseDto);
        return responseDto;
    }

    // 카테고리 별로 랜덤 문제 두 문제씩 가져오기
    @Override
    public ResponseGetRandomQuizzesByCategoriesDto getRandomQuizzesByCategories() {
        log.info("카테고리 별로 랜덤 문제 두 문제씩 가져오기 요청");
        List<Category> categories = categoryRepository.findAll();
        ResponseGetRandomQuizzesByCategoriesDto responseDto = ResponseGetRandomQuizzesByCategoriesDto.builder()
                .randomQuizList(new ArrayList<>())
                .build();

        for (Category category : categories) {

            // 카테고리 별로 랜덤 문제 두 문제씩 가져오기
            List<Quiz> randomQuizzes = quizRepository.findRandomQuizByCategoryId(category.getId(), 2);

            for (Quiz randomQuiz : randomQuizzes) {

                if (randomQuiz.getType() == QuizType.CHOICE) {
                    responseDto.getRandomQuizList().add(GetChoiceAnsQuizDto.builder()
                            .categoryId(randomQuiz.getCategory().getId())
                            .quizId(randomQuiz.getId())
                            .categoryType(randomQuiz.getCategory().getCategory())
                            .name(randomQuiz.getName())
                            .choice1(randomQuiz.getChoiceAnsQuiz().getChoice1())
                            .choice2(randomQuiz.getChoiceAnsQuiz().getChoice2())
                            .choice3(randomQuiz.getChoiceAnsQuiz().getChoice3())
                            .choice4(randomQuiz.getChoiceAnsQuiz().getChoice4())
                            .answer(randomQuiz.getChoiceAnsQuiz().getAnswer())
                            .build()
                    );
                }
                else if (randomQuiz.getType() == QuizType.SHORT) {
                    responseDto.getRandomQuizList().add(GetShortAnsQuizDto.builder()
                            .categoryId(randomQuiz.getCategory().getId())
                            .quizId(randomQuiz.getId())
                            .categoryType(randomQuiz.getCategory().getCategory())
                            .name(randomQuiz.getName())
                            .englishAnswer(randomQuiz.getShortAnsQuiz().getEnglishAnswer())
                            .koreanAnswer(randomQuiz.getShortAnsQuiz().getKoreanAnswer())
                            .build()
                    );
                }
            }
        }
        log.info("카테고리 별로 랜덤 문제 두 문제씩 가져오기 응답 : {}", responseDto);
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
}