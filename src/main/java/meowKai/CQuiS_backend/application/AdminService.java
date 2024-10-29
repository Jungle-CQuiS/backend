package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.RequestCreateNewChoiceAnswerQuiz;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewChoiceAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewShortAnswerQuizDto;

import java.util.List;

public interface AdminService {
    ResponseCreateNewShortAnswerQuizDto createNewShortQuiz(RequestCreateNewShortAnswerQuizDto requestDto);
    ResponseCreateNewChoiceAnswerQuizDto createNewChoiceQuiz(RequestCreateNewChoiceAnswerQuiz requestDto);
    void createNewMultipleShortQuiz(List<RequestCreateNewShortAnswerQuizDto> requestList);
    void createNewMultipleChoiceQuiz(List<RequestCreateNewChoiceAnswerQuiz> requestList);
}