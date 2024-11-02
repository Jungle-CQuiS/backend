//package meowKai.CQuiS_backend.config.websocket;
//
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//@Component
//public class JwtChannelInterceptor implements ChannelInterceptor {
//
//    public static final String AUTHORIZATION = "Authorization";
//    public static final String BEARER = "Bearer ";
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
//            return message;
//        }
//
//        Optional<String> jwtTokenOptional = Optional.ofNullable(accessor.getFirstNativeHeader(AUTHORIZATION));
//        String jwtToken = jwtTokenOptional
//                .filter(token -> token.startsWith(BEARER))
//                .map(token -> token.substring(BEARER.length()))
//                .filter(token -> !isExpired(token))
//                .orElseThrow(() -> new RuntimeException("Invalid token"));
//
//        String userId = getId(jwtToken);
//        String userRole = getRole(jwtToken);
//
//        Authentication authentication = createAuthentication(userId, userRole);
//        accessor.setUser(authentication);
//
//        return message;
//    }
//}
