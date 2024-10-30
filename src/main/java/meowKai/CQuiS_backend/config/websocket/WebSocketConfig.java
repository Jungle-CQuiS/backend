package meowKai.CQuiS_backend.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(
                "/topic",       // 방 전체 관련 구독
                "/queue"       // 개인 에러 메시지
        );
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user"); // 사용자별 메시지에 사용되는 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .setAllowedOrigins("*");
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .setAllowedOrigins("*")
                .withSockJS();

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {     // 메시지 수신되기 전에 검증 및 변환하는 preSend 메소드 재정의
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class); // STOMP 메시지의 헤더 정보를 가져옴

                if(StompCommand.CONNECT.equals(accessor.getCommand())) {    // 연결(CONNECT) 요청시
                    log.debug("유저 인증 상태: {}", accessor.getUser());
                    String uuid = accessor.getFirstNativeHeader("uuid"); // 헤더에서 uuid 가져옴
                    if(uuid != null) {
                        accessor.setUser(new Principal() {  // uuid를 Principal(사용자 식별자)로 설정
                            @Override
                            public String getName() {
                                return uuid;
                            }
                        });
                    }
                }
                return message;
            }
        });
    }
}