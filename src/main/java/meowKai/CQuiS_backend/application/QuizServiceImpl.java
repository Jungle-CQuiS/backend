package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.application.quiz.KoreanAnalyzer;
import meowKai.CQuiS_backend.application.quiz.SimilarityCalculator;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.*;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static meowKai.CQuiS_backend.domain.MultiConstants.MULTI_QUIZZES_PER_CATEGORY;
import static meowKai.CQuiS_backend.domain.MultiConstants.MULTI_QUIZZES_PER_ROUND;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;
    private final QuizUserVotedownRepository quizUserVotedownRepository;
    private final GameRoomRepository gameRoomRepository;
    private final UserRepository userRepository;

    private final Map<Long, List<Object>> roomQuizzes = new ConcurrentHashMap<>(); // 방별로 미리 퀴즈를 할당받아 저장해놓기 위한 ConcurrentHashMap


    @Override
    public ResponseGradeDto checkGrade(RequestGradeDto requestDto) {
        log.info("채점 요청 : {}", requestDto);

        Quiz foundQuiz = quizRepository.findById(requestDto.getQuizId()).orElseThrow(
                () -> new NoSuchElementException("채점 요청 - 존재하지 않는 퀴즈입니다.")
        );

        String userInput = requestDto.getUserInput().replace(" ", ""); // 채점할 사용자의 입력

        boolean isChoice = foundQuiz.getType().equals(QuizType.CHOICE); // 퀴즈가 객관식인지
        boolean isEnglish = userInput.matches("[a-zA-Z]+"); // 영어로만 이루어져 있는지
        if(isEnglish) {
            userInput = userInput.toLowerCase();
        }
        boolean isTrans = KoreanAnalyzer.isTransliteration(userInput); // 음차 표기인지

        String correctAnswer = getCorrectAnswer(foundQuiz, isEnglish);

        double similarity;
        if (isChoice || isEnglish || !isTrans) {
            similarity = correctAnswer.equals(userInput) ? 1.0 : 0.0;   // 음차 표기가 아니라면 정확하게 일치해야 정답
        } else {
            int length = Math.min(correctAnswer.length(), userInput.length());
            boolean isShort = length <= 2;

            if (isShort) {       // 문자열이 짧으면 가중치를 조절해 Jaro-Winkler 거리를 반영하지 않음
                double[] weights = {0.2125, 0.0, 0.4375, 0.2125, 0.1375};
                similarity = SimilarityCalculator.comprehensiveSimilarity(correctAnswer, userInput, weights);
            } else {
                double[] weights = {0.125, 0.35, 0.35, 0.125, 0.05};
                similarity = SimilarityCalculator.comprehensiveSimilarity(correctAnswer, userInput, weights);
            }
        }

        String answer = getAnswer(foundQuiz, isChoice);

        ResponseGradeDto responseDto = similarity >= 0.9 ?
                ResponseGradeDto.builder()
                        .isCorrect(true)
                        .answer(answer)
                        .build()
                : ResponseGradeDto.builder()
                .isCorrect(false)
                .answer(answer)
                .build();

        log.info("채점 완료 : {}", responseDto);
        return responseDto;
    }

    // 문제의 타입과 유저의 입력에 따라 다른 정답을 가져옴
    private String getCorrectAnswer(Quiz quiz, boolean isEnglish) {
        if (quiz.getType() == QuizType.CHOICE) {
            return quiz.getChoiceAnsQuiz().getAnswer().toString();
        }

        return isEnglish ?
                quiz.getShortAnsQuiz().getEnglishAnswer().replace(" ", "").toLowerCase() :
                quiz.getShortAnsQuiz().getKoreanAnswer().replace(" ", "");
    }

    // 화면에 출력하기 위한 형태로 문제의 정답을 반환하기 위한 메소드, 문제의 타입에 따라 형태가 달라짐
    private String getAnswer(Quiz quiz, boolean isChoice) {
        if (isChoice) {
            return quiz.getChoiceAnsQuiz().getAnswer().toString();
        }

        String koreanAnswer = quiz.getShortAnsQuiz().getKoreanAnswer();
        String englishAnswer = quiz.getShortAnsQuiz().getEnglishAnswer();

        // 정답 null 체크
        if (koreanAnswer == null) {
            return englishAnswer;
        } else if (englishAnswer == null) {
            return koreanAnswer;
        }
        return String.format("%s (%s)", koreanAnswer, englishAnswer);
    }

    // 카테고리 정보 가져오기
    @Override
    public ResponseGetCategoriesDto getCategories() {
        log.info("카테고리 정보 요청");
        List<GetCategoryDto> categories = categoryRepository.findAll().stream()
                .map(category -> GetCategoryDto.builder()
                        .categoryId(category.getId())
                        .categoryType(category.getCategory())
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

        User foundUser = userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
                () -> new NoSuchElementException("주관식 문제 요청 - 존재하지 않는 유저입니다.")
        );

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

        ResponseGetShortAnswerQuizzesDto responseDto = ResponseGetShortAnswerQuizzesDto.builder()
                .quizList(new ArrayList<>())
                .build();
        quizzesPerCategory.forEach((key, value) -> {
            // 카테고리 별로 가져와야 할 문제 갯수
            long categoryId = key;
            int count = value;

            // 조건에 해당하는 문제 모두 불러오기
            List<Quiz> quizzesFitConditions = quizRepository
                    .findAllByCategoryIdAndQuizTypeExcludingDownvote(categoryId, foundUser.getId(), count, QuizType.SHORT.toString());

            // 카테고리에 해당하는 문제 갯수보다 요청한 문제의 수가 더 많으면 카테고리의 모든 문제를 가져옴
            if (count >= quizzesFitConditions.size()) {
                quizzesFitConditions.forEach(quiz -> responseDto.getQuizList().add(
                        GetShortAnsQuizDto.createDto(quiz)));
            } else {
                // 문제를 랜덤하게 섞어서 count만큼 가져옴
                Collections.shuffle(quizzesFitConditions);

                List<Quiz> randomQuizzes = quizzesFitConditions.stream()
                        .limit(count)
                        .toList();

                for (Quiz quiz : randomQuizzes) {
                    responseDto.getQuizList().add(
                            GetShortAnsQuizDto.createDto(quiz));
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

        User foundUser = userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
                () -> new NoSuchElementException("주관식 문제 요청 - 존재하지 않는 유저입니다.")
        );

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

        ResponseGetChoiceAnswerQuizzesDto responseDto = ResponseGetChoiceAnswerQuizzesDto.builder()
                .quizList(new ArrayList<>())
                .build();

        quizzesPerCategory.forEach((key, value) -> {

            // 카테고리 별로 가져와야 할 문제 갯수
            long categoryId = key;
            int count = value;

            // 조건에 해당하는 문제 모두 불러오기
            List<Quiz> quizzesFitConditions = quizRepository
                    .findAllByCategoryIdAndQuizTypeExcludingDownvote(categoryId, foundUser.getId(), count, QuizType.CHOICE.toString());

            // 카테고리에 해당하는 문제 갯수보다 요청한 문제의 수가 더 많으면 카테고리의 모든 문제를 가져옴
            if (count >= quizzesFitConditions.size()) {

                quizzesFitConditions.forEach(quiz -> responseDto.getQuizList().add(
                        GetChoiceAnsQuizDto.createDto(quiz)));
            } else {
                // 문제를 랜덤하게 섞어서 count만큼 가져옴
                Collections.shuffle(quizzesFitConditions);

                List<Quiz> randomQuizzes = quizzesFitConditions.stream()
                        .limit(count)
                        .toList();

                for (Quiz quiz : randomQuizzes) {
                    responseDto.getQuizList().add(
                            GetChoiceAnsQuizDto.createDto(quiz));
                }
            }
        });
        log.info("객관식 문제 요청 응답 : {}", responseDto);
        return responseDto;
    }

    @Override
    public ResponseGetMixAnswerQuizzesDto getMixAnswerQuizzesByConditions(RequestGetMixAnswerQuizzesDto requestDto) {
        log.info("주관식 + 객관식 문제 요청 : {}", requestDto);

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

        ResponseGetMixAnswerQuizzesDto responseDto = ResponseGetMixAnswerQuizzesDto.builder()
                .quizList(new ArrayList<>())
                .build();

        quizzesPerCategory.forEach((key, value) -> {

            // 카테고리 별로 가져와야 할 문제 갯수
            long categoryId = key;
            int count = value;

            // 조건에 해당하는 문제 모두 불러오기
            List<Quiz> quizzesFitConditions = quizRepository.findAllByCategoryId(categoryId);

            // 카테고리에 해당하는 문제 갯수보다 요청한 문제의 수가 더 많으면 카테고리의 모든 문제를 가져옴
            if (count >= quizzesFitConditions.size()) {
                quizzesFitConditions.forEach(quiz -> responseDto.getQuizList().add(
                        quiz.getType() == QuizType.CHOICE
                                ? GetRandomChoiceQuizDto.createDto(quiz)
                                : GetRandomShortQuizDto.createDto(quiz)
                ));
            } else {
                // 문제를 랜덤하게 섞어서 count만큼 가져옴
                Collections.shuffle(quizzesFitConditions);

                List<Quiz> randomQuizzes = quizzesFitConditions.stream()
                        .limit(count)
                        .toList();

                for (Quiz quiz : randomQuizzes) {
                    responseDto.getQuizList().add(
                            quiz.getType() == QuizType.CHOICE
                                    ? GetRandomChoiceQuizDto.createDto(quiz)
                                    : GetRandomShortQuizDto.createDto(quiz)
                    );
                }
            }
        });
        log.info("객관식 문제 요청 응답 : {}", responseDto);
        return responseDto;
    }

    // (멀티 게임 전용) 카테고리 별로 랜덤 문제 두 문제씩 가져오기
    @Override
    public ResponseGetRandomQuizzesByCategoriesDto getRandomQuizzesByCategories(Long roomId) {
        log.info("카테고리 별로 랜덤 문제 두 문제씩 가져오기 요청 - roomId: {}", roomId);

        GameRoom foundRoom = gameRoomRepository.findById(roomId).orElseThrow(
                () -> new NoSuchElementException("랜덤 문제 두 문제씩 가져오기 - 존재하지 않는 방입니다."));
        Integer roundIdx = foundRoom.getQuizCount(); // 현재까지 진행된 문제 수를 인덱스로 사용

        List<Object> allQuizzes = roomQuizzes.get(foundRoom.getId());
        if (allQuizzes == null || allQuizzes.isEmpty()) {
            throw new IllegalStateException("퀴즈를 불러오는데 실패했습니다.");
        }

        List<Category> categories = categoryRepository.findAll();

        List<Object> transferQuizzes = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            int startIdx = i * MULTI_QUIZZES_PER_CATEGORY + roundIdx * MULTI_QUIZZES_PER_ROUND;

            // TODO: 추후 제거할 것, 문제 수가 부족할 경우 처음 문제부터 가져오도록 함 (카테고리가 지켜지지 않거나 나왔던 문제가 다시 나올 수 있음)
            startIdx = startIdx % allQuizzes.size();
            int nextIdx = (startIdx + 1) % allQuizzes.size();

            if (startIdx + 1 >= allQuizzes.size()) {
                log.error("카테고리 별로 랜덤 문제 두 문제씩 가져오기 - 인덱스 범위 초과 roundIdx: {}, startIdx: {}", roundIdx, startIdx);
                throw new IllegalStateException("카테고리 별로 랜덤 문제 두 문제씩 가져오기 - 사용할 수 있는 문제가 없습니다.");
            }

            transferQuizzes.add(allQuizzes.get(startIdx));
            transferQuizzes.add(allQuizzes.get(nextIdx));
        }

        ResponseGetRandomQuizzesByCategoriesDto responseDto = ResponseGetRandomQuizzesByCategoriesDto.builder()
                .randomQuizList(transferQuizzes)
                .build();

        log.info("카테고리 별로 랜덤 문제 두 문제씩 가져오기 응답 : {}", responseDto);
        return responseDto;
    }

    // 게임 시작 직전 호출되어 카테고리별로 20개의 문제를 저장해 둠
    @Override
    public void storeQuizzes(GameRoom gameRoom) {
        log.info("문제 리스트 저장 호출 : {}", gameRoom);

        List<Object> quizzes = new ArrayList<>();
        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {

            // 카테고리 별로 랜덤 문제 20문제씩 가져오기
            List<Quiz> randomQuizzes = quizRepository.findRandomQuizByCategoryId(category.getId(), MULTI_QUIZZES_PER_CATEGORY);

            for (Quiz randomQuiz : randomQuizzes) {

                if (randomQuiz.getType() == QuizType.CHOICE) {
                    quizzes.add(GetChoiceAnsQuizDto.createDto(randomQuiz));
                } else {
                    quizzes.add(GetShortAnsQuizDto.createDto(randomQuiz));
                }
            }
        }
        roomQuizzes.put(gameRoom.getId(), Collections.synchronizedList(quizzes));
        log.info("문제 리스트 저장 완료");
    }

    // 문제 비추천하기(triggered by 별로에요 버튼)
    @Override
    @Transactional
    public void downvoteQuiz(RequestDownvoteDto requestDto) {
        log.info("문제 비추천하기 요청 : {}", requestDto);
        Quiz foundQuiz = quizRepository.findById(requestDto.getQuizId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 퀴즈입니다.")
        );
        User foundUser = userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다.")
        );

        quizUserVotedownRepository.findByUserAndQuiz(foundUser, foundQuiz).ifPresent(
                quizUserVotedown -> {
                    throw new IllegalStateException("이미 비추천한 문제입니다.");
                }
        );
        foundQuiz.downvote();
        QuizUserVotedown quizUserVotedown = QuizUserVotedown.createEntity(foundQuiz, foundUser);
        quizUserVotedownRepository.save(quizUserVotedown);
        log.info("문제 비추천하기 완료");
    }

    @Override
    public ResponseGetMyQuizzesDto getMyQuizzes(RequestGetMyQuizzesDto requestDto, QuizType quizType) {
        log.info("내가 만든 문제 반환 요청 : {}", requestDto);

        User foundUser = userRepository.findByUuidWithCreatedQuizzes(
                requestDto.getUuid(),
                requestDto.getCategoryIds(),
                quizType)
                .orElseThrow(() -> new NoSuchElementException("내가 만든 문제 반환 요청 - 존재하지 않는 유저입니다."));

        ResponseGetMyQuizzesDto responseDto = ResponseGetMyQuizzesDto.builder()
                .quizList(new ArrayList<>())
                .build();

        foundUser.getCreatedQuizzes().forEach(quiz -> responseDto.getQuizList().add(
                quiz.getType() == QuizType.CHOICE
                        ? GetRandomChoiceQuizDto.createDto(quiz)
                        : GetRandomShortQuizDto.createDto(quiz)));

        log.info("내가 만든 문제 반환 결과 : {}", responseDto);
        return responseDto;
    }
}