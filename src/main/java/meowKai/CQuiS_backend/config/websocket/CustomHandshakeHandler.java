package meowKai.CQuiS_backend.config.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.isAuthenticated()) {
            return authentication;
        } else {
            String uuid = request.getHeaders().getFirst("uuid");
            if (uuid == null) {
                uuid = "annoymous-" + UUID.randomUUID().toString();
            }
            return new StompPrincipal(uuid);
        }
    }
}

class StompPrincipal implements Principal {
    private final String name;

    public StompPrincipal(String name) {
        this.name = name != null ? name : "annoymous";
    }

    @Override
    public String getName() {
        return name;
    }
}
