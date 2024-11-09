package meowKai.CQuiS_backend.application;

import io.openvidu.java.client.Session;

public interface OpenViduService {
    Session createSession(); // 세션 생성
    Session getSession(String sessionId); // 특정 방의 세션 가져오기
    String createToken(String sessionId, String userId); // 토큰 생성 & 연결
    void closeSession(String sessionId); // 세션 종료
    void closeConnection(String sessionId, String userId); // 연결 종료
}
