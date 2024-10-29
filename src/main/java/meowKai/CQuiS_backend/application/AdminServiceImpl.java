package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewChoiceAnswerQuiz;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewChoiceAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.infrastructure.CategoryRepository;
import meowKai.CQuiS_backend.infrastructure.ChoiceAnsQuizRepository;
import meowKai.CQuiS_backend.infrastructure.QuizRepository;
import meowKai.CQuiS_backend.infrastructure.ShortAnsQuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final ShortAnsQuizRepository shortAnsQuizRepository;
    private final ChoiceAnsQuizRepository choiceAnsQuizRepository;

    // 주관식 퀴즈 생성하기
    @Override
    public ResponseCreateNewShortAnswerQuizDto createNewShortQuiz(RequestCreateNewShortAnswerQuizDto requestDto) {
        log.info("주관식 퀴즈 생성 요청 : {}", requestDto);
        Category foundCategory = categoryRepository.findByCategory(requestDto.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        // 퀴즈 생성
        Quiz quiz = Quiz.builder()
                .name(requestDto.getName())
                .category(foundCategory)
                .build();
        Quiz savedQuiz = quizRepository.save(quiz);
        foundCategory.addQuiz(savedQuiz);

        // 주관식 퀴즈 생성
        ShortAnsQuiz shortAnsQuiz = ShortAnsQuiz.builder()
                .quiz(savedQuiz)
                .englishAnswer(requestDto.getEnglishAnswer())
                .koreanAnswer(requestDto.getKoreanAnswer())
                .build();
        shortAnsQuizRepository.save(shortAnsQuiz);

        ResponseCreateNewShortAnswerQuizDto responseDto = ResponseCreateNewShortAnswerQuizDto
                .builder()
                .quizId(savedQuiz.getId())
                .name(savedQuiz.getName())
                .koreanAnswer(shortAnsQuiz.getKoreanAnswer())
                .englishAnswer(shortAnsQuiz.getEnglishAnswer())
                .category(foundCategory.getCategory())
                .build();
        log.info("주관식 퀴즈 생성 완료 : {}", responseDto);
        return responseDto;
    }

    // 객관식 퀴즈 생성하기
    @Override
    public ResponseCreateNewChoiceAnswerQuizDto createNewChoiceQuiz(RequestCreateNewChoiceAnswerQuiz requestDto) {
        log.info("객관식 퀴즈 생성 요청 : {}", requestDto);
        Category foundCategory = categoryRepository.findByCategory(requestDto.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        // 퀴즈 생성
        Quiz quiz = Quiz.builder()
                .name(requestDto.getName())
                .category(foundCategory)
                .build();
        Quiz savedQuiz = quizRepository.save(quiz);
        foundCategory.addQuiz(savedQuiz);

        // 객관식 퀴즈 생성하기
        ChoiceAnsQuiz choiceAnsQuiz = ChoiceAnsQuiz.builder()
                .quiz(savedQuiz)
                .choice1(requestDto.getChoice1())
                .choice2(requestDto.getChoice2())
                .choice3(requestDto.getChoice3())
                .choice4(requestDto.getChoice4())
                .answer(requestDto.getAnswer())
                .build();
        choiceAnsQuizRepository.save(choiceAnsQuiz);

        ResponseCreateNewChoiceAnswerQuizDto responseDto = ResponseCreateNewChoiceAnswerQuizDto
                .builder()
                .quizId(savedQuiz.getId())
                .name(savedQuiz.getName())
                .answer(choiceAnsQuiz.getAnswer())
                .category(foundCategory.getCategory())
                .build();
        log.info("객관식 퀴즈 생성 완료 : {}", responseDto);
        return responseDto;
    }

    @Override
    public void createNewMultipleShortQuiz(List<RequestCreateNewShortAnswerQuizDto> requestList) {
        for (RequestCreateNewShortAnswerQuizDto request : requestList) {
            createNewShortQuiz(request);
        }
    }

    @Override
    public void createNewMultipleChoiceQuiz(List<RequestCreateNewChoiceAnswerQuiz> requestList) {
        for (RequestCreateNewChoiceAnswerQuiz request : requestList) {
            createNewChoiceQuiz(request);
        }
    }
}