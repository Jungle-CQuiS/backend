package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.RequestCreateNewMultipleAnswerQuiz;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewMultipleAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewShortAnswerQuizDto;

import java.util.List;

public interface AdminService {
    ResponseCreateNewShortAnswerQuizDto createNewShortQuiz(RequestCreateNewShortAnswerQuizDto requestDto);
    ResponseCreateNewMultipleAnswerQuizDto createNewMultipleQuiz(RequestCreateNewMultipleAnswerQuiz requestDto);
    void createNewMultipleShortQuiz(List<RequestCreateNewShortAnswerQuizDto> requestList);
    void createNewMultipleMultipleQuiz(List<RequestCreateNewMultipleAnswerQuiz> requestList);
}