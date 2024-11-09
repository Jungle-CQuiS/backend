package meowKai.CQuiS_backend.application;

import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OpenViduServiceImpl implements OpenViduService{

    private final OpenVidu openVidu;

    /**
     * 세션 생성
     */
    @Override
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
    @Override
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
    @Override
    public String createToken(String sessionId, String userId) {
        try {
            Session session = getSession(sessionId);

            ConnectionProperties properties = new ConnectionProperties.Builder()
                    .type(ConnectionType.WEBRTC)
                    .role(OpenViduRole.PUBLISHER)
                    .data(userId)
                    .build();

            Connection connection = session.createConnection(properties);
            String fullToken = connection.getToken();

            return fullToken.substring(fullToken.indexOf("token=") + 6);
        }  catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("연결 실패", e);
            throw new RuntimeException("연결 실패", e);
        }
    }

    /**
     * 세션 종료 - 방이 삭제될 때
     */
    @Override
    public void closeSession(String sessionId) {
        try {
            Session session = openVidu.getActiveSession(sessionId);
            if(session != null) {
                session.close();
            }
        }  catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("세션 종료 실패", e);
            throw new RuntimeException("세션 종료 실패", e);
        }
    }

    /**
     * 연결 종료 - 유저가 방에서 나갈 때
     */
    public void closeConnection(String sessionId, String userId) {
        Session session = getSession(sessionId);
        session.getConnections()
                .stream()
                .filter(conn -> conn.getServerData().equals(userId))
                .forEach(conn -> {
                    try {
                        session.forceDisconnect(conn);
                    } catch (Exception e) {
                        log.error("연결 종료 실패: {}, {}",userId, e.getMessage());
                    }
                });
    }
}
