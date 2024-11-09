package meowKai.CQuiS_backend.application;

import io.openvidu.java.client.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
public class OpenViduServiceImpl implements OpenViduService{

    private OpenVidu openVidu;

    @Value("${openvidu.url}")
    private String OPENVIDU_URL = "";

    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET = "";

    @PostConstruct
    public void init() {
        this.openVidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    /**
     * 세션 생성
     */
    public Session createSession() {
        try {
            SessionProperties properties = new SessionProperties.Builder().build();
            return openVidu.createSession(properties);
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("세션 생성 실패", e);
            throw new RuntimeException("세션 생성 실패", e);
        }
    }

    /**
     * 특정 방의 세션 가져오기
     */
    public Session getSession(String sessionId) {
        Session session = openVidu.getActiveSession(sessionId);
        if (session == null) {
            log.error("세션 찾기 실패 " + sessionId);
            throw new RuntimeException("세션 찾기 실패 " + sessionId);
        }
        return session;
    }

    /**
     * 토큰 생성 & 연결
     */
    public String createToken(String sessionId, String userId) {
        try {
            Session session = getSession(sessionId);

            ConnectionProperties properties = new ConnectionProperties.Builder()
                    .type(ConnectionType.WEBRTC)
                    .role(OpenViduRole.PUBLISHER)
                    .data(userId)
                    .build();

            Connection connection = session.createConnection(properties);
            return connection.getToken();
        }  catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("연결 실패", e);
            throw new RuntimeException("연결 실패", e);
        }
    }

    /**
     * 세션 종료
     */
    public void closeSession(String sessionId) {
        try {
            Session session = getSession(sessionId);
            session.close();
        }  catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("세션 종료 실패", e);
            throw new RuntimeException("세션 종료 실패", e);
        }
    }
}
