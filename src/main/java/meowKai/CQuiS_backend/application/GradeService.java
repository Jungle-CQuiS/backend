package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.RequestGradeDto;
import meowKai.CQuiS_backend.dto.response.ResponseGradeDto;

public interface GradeService {
    ResponseGradeDto checkGrade(RequestGradeDto requestGradeDto); // 채점
}
