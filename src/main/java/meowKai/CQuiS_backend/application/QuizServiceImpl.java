package meowKai.CQuiS_backend.application;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.dto.request.RequestGradeDto;
import meowKai.CQuiS_backend.dto.response.ResponseGradeDto;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.python.core.*;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    private PythonInterpreter interpreter; // 파이썬 인터프리터, 파이썬 코드 실행하고 결과 확인
    //private final QuizRepository quizRepository; // TODO: 문제 정보를 가져오기 위한 문제 관련 repository

    private PyFunction comprehensiveGrade; // Python 코드에서 가져옴 - 종합 유사도를 구하는 함수
    private PyFunction isTransliteration; // Python 코드에서 가져옴 - 사용자의 입력이 음차 표기인지 판단하는 함수

    // Jython을 위한 세팅
    @PostConstruct
    public void init() {
        System.setProperty("python.import.site", "false");
        interpreter = new PythonInterpreter(); // Python 인터프리터 생성

        codecs.setDefaultEncoding("utf-8");

        // 인코딩 설정
        interpreter.exec("import sys");
        interpreter.exec("reload(sys)");
        interpreter.exec("sys.setdefaultencoding('utf-8')");

        // Python 스크립트 로드
        try (InputStream is = getClass().getResourceAsStream("/python/grading.py")) {
            if(is == null) {
                throw new RuntimeException("Python script not found");
            }
            interpreter.execfile(is);

            // 필요한 함수 가져옴
            comprehensiveGrade = (PyFunction) interpreter.get("comprehensive_grade");
            isTransliteration = (PyFunction) interpreter.get("is_transliteration");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load Python script", e);
        }
    }

    @Override
    public ResponseGradeDto checkGrade(RequestGradeDto requestGradeDto) {
        log.info("채점 요청 : {}", requestGradeDto);

        // 공백 제거 전처리
        String correctAnswer = "정답"; // TODO: 문제 관련 Repository에서 정답 가져오기
        String userInput = requestGradeDto.getUserInput().replace(" ", "");

        // 영어 음차 표기 여부 확인
        boolean isTrans = isTransliteration.__call__(new PyUnicode(userInput)).__nonzero__();

        double similarity;
        if(!isTrans) {
            similarity = correctAnswer.equals(userInput) ? 1.0 : 0.0;   // 음차 표기가 아니라면 정확하게 일치해야 정답
        } else {
            int length = Math.min(correctAnswer.length(), userInput.length());
            boolean isShort = length <= 2;

            if(isShort) {       // 문자열이 짧으면 가중치를 조절해 Jaro-Winkler 거리를 반영하지 않음
                double[] weights = {0.2125, 0.0, 0.4375, 0.2125, 0.1375};
                similarity = calculateGrade(correctAnswer, userInput, weights);
            } else {
                double[] weights = {0.125, 0.35, 0.35, 0.125, 0.05};
                similarity = calculateGrade(correctAnswer, userInput, weights);
            }
        }

        ResponseGradeDto responseDto = similarity >= 0.9 ? ResponseGradeDto.builder().isCorrect(true).build() : ResponseGradeDto.builder().isCorrect(false).build();
        log.info("채점 완료 : {}", responseDto);
        return responseDto;
    }

    // Python 함수를 호출하고 유사도를 반환받는 함수
    private double calculateGrade(String correctAnswer, String userInput, double[] weights) {
        interpreter.exec("from array import array");    // Python array 모듈 import
        interpreter.set("weights", weights);    // Java의 double 배열 Python으로 전달
        interpreter.exec("weights_list = list(weights)");   // weights를 Python List로 변환

        PyObject grade = comprehensiveGrade.__call__(
                new PyUnicode(correctAnswer),       //Java의 String을 PyUnicode로 변환, 한글 처리를 위해 유니코드로 변환함
                new PyUnicode(userInput),
                interpreter.get("weights_list")     // 반환된 List(Python의 List)를 가져옴
        );

        return Double.parseDouble(grade.toString());    // PyObject 형태의 결과를 Java double 타입으로 변환하며 반홚
    }



    @PreDestroy
    public void cleanUp() {
        if(interpreter != null) {
            interpreter.close();    // 종료 전에 Python 인터프리터 리소스 해제
        }
    }
}
