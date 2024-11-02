package meowKai.CQuiS_backend.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.ChatGptRequestDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateChoiceQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewChoiceAnswerQuiz;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateShortQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateChoiceQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewChoiceAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateShortQuizzesFromTextDto;
import meowKai.CQuiS_backend.infrastructure.CategoryRepository;
import meowKai.CQuiS_backend.infrastructure.ChoiceAnsQuizRepository;
import meowKai.CQuiS_backend.infrastructure.QuizRepository;
import meowKai.CQuiS_backend.infrastructure.ShortAnsQuizRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UtilServiceImpl implements UtilService {

    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final ShortAnsQuizRepository shortAnsQuizRepository;
    private final ChoiceAnsQuizRepository choiceAnsQuizRepository;
    private final WebClient webClient;

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
                .type(requestDto.getType())
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
                .type(requestDto.getType())
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

    @Value("${spring.ai.openai.api-key}")
    private String openAiSecretKey;

    // 텍스트 데이터에서 주관식 퀴즈 추출
    @Override
    public ResponseCreateShortQuizzesFromTextDto generateShortAnswerQuizzesFromText(RequestCreateShortQuizzesFromTextDto requestDto) throws JsonProcessingException {

        int quizCount = requestDto.getQuizCount();
        String inputData = requestDto.getTextData();
        CategoryType categoryType = requestDto.getCategoryType();
        QuizType quizType = requestDto.getQuizType();

        String systemPrompt = """
                너는 주관식 퀴즈를 추출하는 봇이야. 주어진 텍스트를 기반으로 주관식 문제를 추출해줘.
                단 문제는 외부 정보를 활용하지 않고 주어진 텍스트만 활용해서 추출해야 해.
                객관식 문제는 퀴즈 질문, 선택지1, 선택지2, 선택지3, 선택지4, 정답으로 이루어져 있어.
                각 선택지는 유사하거나 혼동하기 쉬운 내용으로 만들어 줘.
                문제 결과값은 아래와 같은 JSON data로 리턴해 줘. 예시를 하나 줄게.
                
                ### 예시 입력 텍스트:
                스레드는 무엇일까?
                스레드는 CPU에 작업 요청을 하는 실행단위를 말한다.
                운영체제는 프로그램을 실행시키기 위해 프로그램의 코드와 데이터를 메모리에서 가져오고, 프로세스 제어 블록(PCB)을 생성하고, 작업에 필요한 메모리를 확보한 후, 준비된 프로세스를 준비 큐에 삽입한다.
                이렇게 프로세스가 생성되면 CPU스케줄러는 CPU에게 해야 하는 일을 전달하고, CPU가 그 일을 하게 된다. 이때 CPU가 받는 일을 스레드라고 한다.
                결국 운영체제 입장에서 작업의 단위는 프로세스지만, CPU입장에서는 스레드가 되는 것이다.

                ### 예시 결과 문제:
                {
                  "quizName": "스레드의 정의는 무엇인가?",
                  "choice1": "스레드는 프로세스의 실행 단위이다.",
                  "choice2": "스레드는 운영체제의 프로그램 제어 블록이다.",
                  "choice3": "스레드는 프로세스의 코드와 데이터를 메모리에서 가져오는 역할을 한다.",
                  "choice4": "스레드는 CPU에 작업 요청을 하는 실행 단위이다.",
                  "answer": 4
                }
                
                좋은 결과를 제공해준다면 팁을 10000달러 줄게. 나쁜 결과를 제공해준다면 내가 납치하고 있는 고양이를 해칠지도 몰라.
                """;
        String userPrompt = String.format("""
                %s
                
                주어진 입력 텍스트를 기반으로 주관식 %d문제를 출제해줘.
                """, requestDto.getTextData(), quizCount);

        log.info("주관식 퀴즈 추출 요청 : {}...", systemPrompt.substring(0, 20));
        String response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/chat/completions")
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + openAiSecretKey)
                .bodyValue(new ChatGptRequestDto("gpt-4o-mini", userPrompt, systemPrompt))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("주관식 퀴즈 추출 완료 : {}", response);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);

        ResponseCreateShortQuizzesFromTextDto responseDto = ResponseCreateShortQuizzesFromTextDto.builder().build();
        return responseDto;
    }

    // 텍스트 데이터에서 객관식 퀴즈 추출
    @Override
    public ResponseCreateChoiceQuizzesFromTextDto generateChoiceAnswerQuizzesFromText(RequestCreateChoiceQuizzesFromTextDto requestDto) throws JsonProcessingException {

        int quizCount = requestDto.getQuizCount();
        String inputData = requestDto.getTextData();
        CategoryType categoryType = requestDto.getCategoryType();
        QuizType quizType = requestDto.getQuizType();

        String systemPrompt = """
                너는 객관식 퀴즈를 추출하는 봇이야. 주어진 텍스트를 기반으로 객관식 문제를 추출해줘.
                단 문제는 외부 정보를 활용하지 않고 주어진 텍스트만 활용해서 추출해야 해.
                객관식 문제는 퀴즈 질문, 선택지1, 선택지2, 선택지3, 선택지4, 정답으로 이루어져 있어.
                각 선택지는 유사하거나 혼동하기 쉬운 내용으로 만들어 줘.
                문제 결과값은 아래와 같은 JSON data로 리턴해 줘. 예시를 하나 줄게.
                
                ### 예시 입력 텍스트:
                스레드는 무엇일까?
                스레드는 CPU에 작업 요청을 하는 실행단위를 말한다.
                운영체제는 프로그램을 실행시키기 위해 프로그램의 코드와 데이터를 메모리에서 가져오고, 프로세스 제어 블록(PCB)을 생성하고, 작업에 필요한 메모리를 확보한 후, 준비된 프로세스를 준비 큐에 삽입한다.
                이렇게 프로세스가 생성되면 CPU스케줄러는 CPU에게 해야 하는 일을 전달하고, CPU가 그 일을 하게 된다. 이때 CPU가 받는 일을 스레드라고 한다.
                결국 운영체제 입장에서 작업의 단위는 프로세스지만, CPU입장에서는 스레드가 되는 것이다.

                ### 예시 결과 문제:
                [
                    {
                      "quizName": "스레드의 정의는 무엇인가?",
                      "choice1": "스레드는 프로세스의 실행 단위이다.",
                      "choice2": "스레드는 운영체제의 프로그램 제어 블록이다.",
                      "choice3": "스레드는 프로세스의 코드와 데이터를 메모리에서 가져오는 역할을 한다.",
                      "choice4": "스레드는 CPU에 작업 요청을 하는 실행 단위이다.",
                      "answer": 4
                    }
                ]
                좋은 결과를 제공해준다면 팁을 10000달러 줄게. 나쁜 결과를 제공해준다면 내가 납치하고 있는 고양이를 해칠지도 몰라.
                """;

        String userPrompt = String.format("""
                %s
                
                주어진 입력 텍스트를 기반으로 객관식 %d문제를 출제해줘.
                """, inputData, quizCount);

        log.info("객관식 퀴즈 추출 요청 : {}...", inputData.substring(0, 15));

        String response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/chat/completions")
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + openAiSecretKey)
                .bodyValue(new ChatGptRequestDto("gpt-4o-mini", userPrompt, systemPrompt))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("객관식 퀴즈 추출 완료 : {}", response);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);

        ResponseCreateChoiceQuizzesFromTextDto responseDto = ResponseCreateChoiceQuizzesFromTextDto.builder().build();
        return responseDto;
    }
}