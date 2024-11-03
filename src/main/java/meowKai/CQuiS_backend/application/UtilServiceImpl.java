package meowKai.CQuiS_backend.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.request.RequestCreateChoiceQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewChoiceAnswerQuiz;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateShortQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateChoiceQuizFromTextDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewChoiceAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateShortQuizFromTextDto;
import meowKai.CQuiS_backend.infrastructure.CategoryRepository;
import meowKai.CQuiS_backend.infrastructure.ChoiceAnsQuizRepository;
import meowKai.CQuiS_backend.infrastructure.QuizRepository;
import meowKai.CQuiS_backend.infrastructure.ShortAnsQuizRepository;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private static final String DEFAULT_CHAT_URL = "https://api.openai.com";

    private static final String CHOICE_QUIZ_PROMPT =
            """
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
                
                Pintos에서 sema_up()과 sema_down()은 스레드 간의 동기화 및 자원 관리를 위한 세마포어를 사용하는 중요한 함수입니다.
                세마포어는 공유 자원에 대한 접근을 조정하고 경쟁 조건을 방지하는 역할을 합니다.
                sema_down()은 세마포어의 값을 감소시키고, 만약 값이 0이면 호출한 스레드를 블록 상태로 전환하여 대기열에 추가합니다.
                이 함수가 호출되면, 세마포어의 현재 값이 1 이상일 경우 값을 1 감소시키고, 값이 0이면 대기 중인 스레드는 자원을 사용할 수 없다는 의미로 대기하게 됩니다.
                반대로, sema_up()은 세마포어의 값을 증가시키고, 블록 상태에 있는 스레드 중 하나를 깨워 실행할 수 있도록 합니다.
                이 함수가 호출되면 세마포어의 현재 값을 1 증가시키고, 대기 중인 스레드가 있다면 대기열에서 하나의 스레드를 선택하여 깨웁니다.
                세마포어는 주로 임계 영역 보호와 자원 접근 조정에 사용되며, 초기화 후 스레드가 자원에 접근할 때 sema_down()을 호출하고,
                자원 사용이 끝난 후 sema_up()을 호출하여 자원을 해제하는 방식으로 작동합니다. 이러한 과정을 통해 세마포어는 스레드 간의 동기화 및 자원 관리를 효과적으로 수행합니다.

                ### 예시 결과 문제:
                [
                    {
                      "quizName": "스레드의 정의는 무엇인가?",
                      "choice1": "스레드는 프로세스의 실행 단위이다.",
                      "choice2": "스레드는 운영체제의 프로그램 제어 블록이다.",
                      "choice3": "스레드는 프로세스의 코드와 데이터를 메모리에서 가져오는 역할을 한다.",
                      "choice4": "스레드는 CPU에 작업 요청을 하는 실행 단위이다.",
                      "answer": 4
                    },
                    {
                      "quizName": "스레드 동기화란 무엇인가?",
                      "choice1": "스레드 간의 실행 순서를 정하는 것",
                      "choice2": "공유 자원에 대한 접근을 제어하는 것",
                      "choice3": "스레드가 메모리를 공유하도록 하는 것",
                      "choice4": "스레드의 우선 순위를 정하는 것",
                      "answer": 2
                    },
                    {
                      "quizName": "Pintos에서 세마포어의 초기 값이 0인 경우, sema_down()을 호출하면 어떤 결과가 발생하는가?",
                      "choice1": "즉시 리턴한다.",
                      "choice2": "스레드는 블록되고 대기열에 추가된다.",
                      "choice3": "오류가 발생한다.",
                      "choice4": "세마포어의 값을 1로 설정한다.",
                      "answer": 2
                    }
                ]
                
                ### 유저 입력 텍스트:
                %s
                
                주어진 입력 텍스트를 기반으로 객관식 %d문제를 출제해줘. 퀴즈의 질문은 위에서 준 예시와 반드시 비슷할 필요는 없어.
                단, 질문과 선택지는 반드시 한국어로 생성해 줘야 해. 대체할 수 없는 단어가 있는 경우에는 영어로 표기해도 괜찮아.
                좋은 결과를 제공해준다면 팁을 10000달러 줄게. 나쁜 결과를 제공해준다면 내가 납치하고 있는 고양이를 해칠지도 몰라.
                """;

    private static final String SHORT_QUIZ_PROMPT =
            """
                너는 주관식 퀴즈를 추출하는 봇이야. 주어진 텍스트를 기반으로 주관식 문제를 추출해줘.
                단 문제는 외부 정보를 활용하지 않고 주어진 텍스트만 활용해서 추출해야 해.
                주관식 문제는 퀴즈 질문, 한국어 정답, 영어 정답으로 이루어져 있어. 단 정답은 단어로 제공해야 해.
                문제 결과값은 아래와 같은 JSON data로 리턴해 줘. 예시를 하나 줄게.
                
                ### 예시 입력 텍스트:
                스레드는 무엇일까?
                스레드는 CPU에 작업 요청을 하는 실행단위를 말한다.
                운영체제는 프로그램을 실행시키기 위해 프로그램의 코드와 데이터를 메모리에서 가져오고, 프로세스 제어 블록(PCB)을 생성하고, 작업에 필요한 메모리를 확보한 후, 준비된 프로세스를 준비 큐에 삽입한다.
                이렇게 프로세스가 생성되면 CPU스케줄러는 CPU에게 해야 하는 일을 전달하고, CPU가 그 일을 하게 된다. 이때 CPU가 받는 일을 스레드라고 한다.
                결국 운영체제 입장에서 작업의 단위는 프로세스지만, CPU입장에서는 스레드가 되는 것이다.
                
                Pintos에서 sema_up()과 sema_down()은 스레드 간의 동기화 및 자원 관리를 위한 세마포어를 사용하는 중요한 함수입니다.
                세마포어는 공유 자원에 대한 접근을 조정하고 경쟁 조건을 방지하는 역할을 합니다.
                sema_down()은 세마포어의 값을 감소시키고, 만약 값이 0이면 호출한 스레드를 블록 상태로 전환하여 대기열에 추가합니다.
                이 함수가 호출되면, 세마포어의 현재 값이 1 이상일 경우 값을 1 감소시키고, 값이 0이면 대기 중인 스레드는 자원을 사용할 수 없다는 의미로 대기하게 됩니다.
                반대로, sema_up()은 세마포어의 값을 증가시키고, 블록 상태에 있는 스레드 중 하나를 깨워 실행할 수 있도록 합니다.
                이 함수가 호출되면 세마포어의 현재 값을 1 증가시키고, 대기 중인 스레드가 있다면 대기열에서 하나의 스레드를 선택하여 깨웁니다.
                세마포어는 주로 임계 영역 보호와 자원 접근 조정에 사용되며, 초기화 후 스레드가 자원에 접근할 때 sema_down()을 호출하고,
                자원 사용이 끝난 후 sema_up()을 호출하여 자원을 해제하는 방식으로 작동합니다. 이러한 과정을 통해 세마포어는 스레드 간의 동기화 및 자원 관리를 효과적으로 수행합니다.
                
                ### 예시 결과 문제:
                [
                    {
                      "quizName": "스레드란 무엇인가?",
                      "koreanAnswer": "실행단위",
                      "englishAnswer": "Execution Unit"
                    },
                    {
                      "quizName": "운영체제가 생성하는 구조체는 무엇인가?",
                      "koreanAnswer": "",
                      "englishAnswer": "PCB"
                    },
                    {
                      "quizName": "세마포어는 무엇을 보호하고 조정하는 데 사용되는가?",
                      "koreanAnswer": "임계영역",
                      "englishAnswer": "Critical Section"
                    }
                ]
                
                ### 유저 입력 텍스트:
                %s
                
                주어진 입력 텍스트를 기반으로 주관식 %d문제를 출제해줘. 퀴즈의 질문은 위에서 준 예시와 반드시 비슷할 필요는 없어.
                단 질문은 반드시 한국어로 제공해 생성해 줘야 해. 답의 경우 한국어와 영어로 제공하되, 대체할 수 있는 단어가 없을 경우 ""로 제공해 줘도 괜찮아.
                좋은 결과를 제공해준다면 팁을 10000달러 줄게. 나쁜 결과를 제공해준다면 내가 납치하고 있는 고양이를 해칠지도 몰라.
                """;

    // 텍스트 데이터에서 주관식 퀴즈 추출
    @Override
    public List<ResponseCreateShortQuizFromTextDto> generateShortAnswerQuizzesFromText(RequestCreateShortQuizzesFromTextDto requestDto) throws JsonProcessingException {

        int quizCount = requestDto.getQuizCount();
        String inputData = requestDto.getTextData();

        String jsonSchema = """
        {
            "type": "object",
            "properties": {
                "quizzes": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "quizName": { "type": "string" },
                            "koreanAnswer": { "type": "string" },
                            "englishAnswer": { "type": "string" }
                        },
                        "required": ["quizName", "koreanAnswer", "englishAnswer"],
                        "additionalProperties": false
                    }
                }
            },
            "required": ["quizzes"],
            "additionalProperties": false
        }
        """;

        log.info("주관식 퀴즈 추출 요청 : {}...", inputData.substring(0, 15));
        String inputPrompt = String.format(SHORT_QUIZ_PROMPT, inputData, quizCount);

        Prompt prompt = new Prompt(inputPrompt,
                OpenAiChatOptions.builder()
                        .withModel(OpenAiApi.ChatModel.GPT_4_O_MINI)
                        .withResponseFormat(new OpenAiApi.ChatCompletionRequest.ResponseFormat(
                                        OpenAiApi.ChatCompletionRequest.ResponseFormat.Type.JSON_SCHEMA, "result", jsonSchema, true
                                )
                        )
                        .build());

        OpenAiChatModel chatModel = new OpenAiChatModel(
                new OpenAiApi(DEFAULT_CHAT_URL, openAiSecretKey)
        );

        ChatResponse chatResponse = chatModel.call(prompt);
        List<ResponseCreateShortQuizFromTextDto> responseDtoList = parseShortQuizResponse(chatResponse);
        log.info("주관식 퀴즈 추출 완료 : {}", responseDtoList.toString());
        return responseDtoList;
    }

    // 텍스트 데이터에서 객관식 퀴즈 추출
    @Override
    public List<ResponseCreateChoiceQuizFromTextDto> generateChoiceAnswerQuizzesFromText(RequestCreateChoiceQuizzesFromTextDto requestDto) throws JsonProcessingException {

        int quizCount = requestDto.getQuizCount();
        String inputData = requestDto.getTextData();

        String jsonSchema = """
        {
            "type": "object",
            "properties": {
                "quizzes": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "quizName": { "type": "string" },
                            "choice1": { "type": "string" },
                            "choice2": { "type": "string" },
                            "choice3": { "type": "string" },
                            "choice4": { "type": "string" },
                            "answer": { "type": "integer" }
                        },
                        "required": ["quizName", "choice1", "choice2", "choice3", "choice4", "answer"],
                        "additionalProperties": false
                    }
                }
            },
            "required": ["quizzes"],
            "additionalProperties": false
        }
        """;

        log.info("객관식 퀴즈 추출 요청 : {}...", inputData.substring(0, 15));
        String inputPrompt = String.format(CHOICE_QUIZ_PROMPT, inputData, quizCount);

        Prompt prompt = new Prompt(inputPrompt,
                OpenAiChatOptions.builder()
                        .withModel(OpenAiApi.ChatModel.GPT_4_O_MINI)
                        .withResponseFormat(new OpenAiApi.ChatCompletionRequest.ResponseFormat(
                                OpenAiApi.ChatCompletionRequest.ResponseFormat.Type.JSON_SCHEMA, "result", jsonSchema, true
                                )
                        )
                        .build());

        OpenAiChatModel chatModel = new OpenAiChatModel(
                new OpenAiApi(DEFAULT_CHAT_URL, openAiSecretKey)
        );

        ChatResponse chatResponse = chatModel.call(prompt);
        List<ResponseCreateChoiceQuizFromTextDto> responseDtoList = parseChoiceQuizResponse(chatResponse);
        log.info("객관식 퀴즈 추출 완료 : {}", responseDtoList.toString());
        return responseDtoList;
    }

    private List<ResponseCreateChoiceQuizFromTextDto> parseChoiceQuizResponse(ChatResponse chatResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // JSON 응답에서 quizzes 배열을 추출
            String jsonData = chatResponse.getResults().get(0).getOutput().getContent();
            JsonNode rootNode = objectMapper.readTree(jsonData);
            JsonNode quizzesNode = rootNode.get("quizzes");

            // quizzes 배열을 List<ResponseCreateChoiceQuizFromTextDto>로 변환
            return objectMapper.convertValue(
                    quizzesNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ResponseCreateChoiceQuizFromTextDto.class)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 매핑 오류.", e);
        }
    }

    private List<ResponseCreateShortQuizFromTextDto> parseShortQuizResponse(ChatResponse chatResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // JSON 응답에서 quizzes 배열을 추출
            String jsonData = chatResponse.getResults().get(0).getOutput().getContent();
            JsonNode rootNode = objectMapper.readTree(jsonData);
            JsonNode quizzesNode = rootNode.get("quizzes");

            // quizzes 배열을 List<ResponseCreateChoiceQuizFromTextDto>로 변환
            return objectMapper.convertValue(
                    quizzesNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ResponseCreateShortQuizFromTextDto.class)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 매핑 오류.", e);
        }
    }
}