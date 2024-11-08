package meowKai.CQuiS_backend.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.config.openai.OpenAiConfig;
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

import java.util.List;

import static meowKai.CQuiS_backend.config.openai.OpenAiConfig.DEFAULT_CHAT_URL;
import static meowKai.CQuiS_backend.config.openai.QuizPrompts.CHOICE_QUIZ_PROMPT;
import static meowKai.CQuiS_backend.config.openai.QuizPrompts.SHORT_QUIZ_PROMPT;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UtilServiceImpl implements UtilService {

    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final ShortAnsQuizRepository shortAnsQuizRepository;
    private final ChoiceAnsQuizRepository choiceAnsQuizRepository;
    private final OpenAiConfig openAiConfig;

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

    // 텍스트 데이터에서 주관식 퀴즈 추출
    @Override
    public List<ResponseCreateShortQuizFromTextDto> generateShortAnswerQuizzesFromText(RequestCreateShortQuizzesFromTextDto requestDto) throws JsonProcessingException {

        int quizCount = requestDto.getQuizCount();
        String inputData = requestDto.getTextData();

        String jsonSchema = openAiConfig.getShortQuizJsonSchema();

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
                new OpenAiApi(DEFAULT_CHAT_URL, openAiConfig.getOpenAiSecretKey())
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

        String jsonSchema = openAiConfig.getChoiceQuizJsonSchema();

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
                new OpenAiApi(DEFAULT_CHAT_URL, openAiConfig.getOpenAiSecretKey())
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