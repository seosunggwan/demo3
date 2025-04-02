package com.example.backend.common.config.websocket;

import com.example.backend.chat.service.ChatService;
import com.example.backend.security.jwt.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Component
public class StompHandler implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(StompHandler.class);

    private final JWTUtil jwtUtil;
    private final ChatService chatService;

    public StompHandler(
        ChatService chatService,
        JWTUtil jwtUtil
    ) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                logger.debug("[STOMP CONNECT] 토큰 검증 시작...");
                
                // 헤더에서 Authorization 토큰 추출
                List<String> authorization = accessor.getNativeHeader("Authorization");
                
                if (authorization != null && !authorization.isEmpty()) {
                    String bearerToken = authorization.get(0);
                    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                        String token = bearerToken.substring(7);
                        logger.debug("JWT 토큰 검증: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                        
                        validateToken(token, accessor);
                    }
                } else {
                    logger.debug("Authorization 헤더가 없거나 비어 있습니다.");
                }
                
                // 쿼리 파라미터에서 토큰 추출 시도 (백업 방식)
                String query = accessor.getFirstNativeHeader("query");
                if (query != null && query.contains("token=")) {
                    String token = extractTokenFromQuery(query);
                    logger.debug("쿼리 파라미터에서 토큰 추출: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                    
                    validateToken(token, accessor);
                }
            }

            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                System.out.println("🔐 [STOMP SUBSCRIBE] principal 기반 권한 확인...");

                // 헤더에서 Authorization 토큰 추출 및 검증
                List<String> authorization = accessor.getNativeHeader("Authorization");
                if (authorization != null && !authorization.isEmpty()) {
                    String bearerToken = authorization.get(0);
                    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                        String token = bearerToken.substring(7);
                        logger.debug("SUBSCRIBE 토큰 검증: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                        validateToken(token, accessor);
                    }
                }

                Principal principal = accessor.getUser();
                if (principal == null) {
                    throw new AuthenticationServiceException("🚨 인증되지 않은 사용자입니다.");
                }
                String email = principal.getName();

                String destination = accessor.getDestination();
                Long roomId = parseRoomId(destination);

                // 채팅방 참여 여부 확인
                if (!chatService.isRoomPaticipant(email, roomId)) {
                    throw new AuthenticationServiceException("🚨 채팅방 권한이 없습니다.");
                }

                System.out.println("✅ [STOMP SUBSCRIBE] 사용자: " + email + ", roomId=" + roomId);
            }

            // SEND 명령어 처리 추가 (채팅 메시지 전송)
            if (StompCommand.SEND.equals(accessor.getCommand())) {
                logger.info("🔔 [STOMP SEND] 메시지 전송 요청 감지");
                
                // 헤더에서 Authorization 토큰 추출 및 검증
                List<String> authorization = accessor.getNativeHeader("Authorization");
                if (authorization != null && !authorization.isEmpty()) {
                    String bearerToken = authorization.get(0);
                    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                        String token = bearerToken.substring(7);
                        logger.debug("SEND 토큰 검증: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                        validateToken(token, accessor);
                    }
                }
                
                Principal principal = accessor.getUser();
                if (principal == null) {
                    logger.error("인증되지 않은 사용자의 메시지 전송 시도");
                    return message;
                }
                
                String email = principal.getName();
                String destination = accessor.getDestination();
                
                // /app/chat/1 또는 /publish/1 형식의 메시지 전송 경로에서 방 ID 추출
                if (destination != null && (destination.startsWith("/app/chat/") || destination.startsWith("/publish/"))) {
                    try {
                        Long roomId;
                        if (destination.startsWith("/app/chat/")) {
                            roomId = Long.parseLong(destination.substring("/app/chat/".length()));
                        } else {
                            roomId = Long.parseLong(destination.substring("/publish/".length()));
                        }
                        logger.info("메시지 전송 확인: 사용자={}, 채팅방={}, 경로={}", email, roomId, destination);
                        
                        // 여기서는 메시지 내용 로깅만 수행 (실제 저장은 컨트롤러에서)
                        logger.debug("메시지 내용: {}", new String((byte[]) message.getPayload()));
                    } catch (Exception e) {
                        logger.error("메시지 처리 중 오류: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("STOMP 메시지 처리 중 오류 발생", e);
        }
        
        return message;
    }
    
    private void validateToken(String token, StompHeaderAccessor accessor) {
        try {
            if (!jwtUtil.isExpired(token)) {
                String username = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);
                logger.debug("토큰 검증 성공! 사용자: {}, 역할: {}", username, role);
                
                // 인증 정보 저장
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(role))
                );
                accessor.setUser(auth);
            } else {
                logger.debug("토큰이 만료되었습니다.");
            }
        } catch (Exception e) {
            logger.error("토큰 검증 오류: {}", e.getMessage());
        }
    }
    
    // 쿼리 문자열에서 토큰 추출
    private String extractTokenFromQuery(String query) {
        int tokenIndex = query.indexOf("token=");
        if (tokenIndex == -1) return null;
        
        String token = query.substring(tokenIndex + 6);
        int endIndex = token.indexOf("&");
        
        if (endIndex != -1) {
            token = token.substring(0, endIndex);
        }
        
        return token;
    }
    
    private Long parseRoomId(String dest) {
        try {
            String[] parts = dest.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            logger.error("방 ID 파싱 중 오류: {}", e.getMessage());
            return null;
        }
    }
}