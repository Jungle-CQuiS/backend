package meowKai.CQuiS_backend.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import meowKai.CQuiS_backend.dto.request.RequestCreateChoiceQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewChoiceAnswerQuizDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateShortQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateChoiceQuizFromTextDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewChoiceAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateShortQuizFromTextDto;

import java.util.List;
import java.util.UUID;

public interface UtilService {
    ResponseCreateNewShortAnswerQuizDto createNewShortQuiz(RequestCreateNewShortAnswerQuizDto.NewShortAnswerQuizDto requestDto, UUID uuid);
    ResponseCreateNewChoiceAnswerQuizDto createNewChoiceQuiz(RequestCreateNewChoiceAnswerQuizDto.NewChoiceAnswerQuizDto requestDto, UUID uuid);
    void createNewMultipleShortQuiz(RequestCreateNewShortAnswerQuizDto requestDto);
    void createNewMultipleChoiceQuiz(RequestCreateNewChoiceAnswerQuizDto requestDto);
    List<ResponseCreateShortQuizFromTextDto> generateShortAnswerQuizzesFromText(RequestCreateShortQuizzesFromTextDto requestDto) throws JsonProcessingException;
    List<ResponseCreateChoiceQuizFromTextDto> generateChoiceAnswerQuizzesFromText(RequestCreateChoiceQuizzesFromTextDto requestDto) throws JsonProcessingException;
}
