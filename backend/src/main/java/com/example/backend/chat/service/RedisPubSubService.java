package com.example.backend.chat.service;

import com.example.backend.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;
    private final ObjectMapper objectMapper;

    public RedisPubSubService(@Qualifier("chatPubSub") StringRedisTemplate stringRedisTemplate, 
                             SimpMessageSendingOperations messageTemplate,
                             ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String channel, String message) {
        System.out.println("🚀 Redis 채널에 메시지 게시: " + channel + ", 메시지: " + message);
        stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    // pattern에는 topic의 이름의 패턴이 담겨있고, 이 패턴을 기반으로 다이나믹한 코딩
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        // UTF-8로 명시적 변환
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        System.out.println("📬 Redis에서 수신한 메시지: " + payload);
        
        try {
            ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);
            
            // UTF-8 인코딩 재확인
            if (chatMessageDto.getMessage() != null) {
                byte[] bytes = chatMessageDto.getMessage().getBytes(StandardCharsets.UTF_8);
                String utf8Message = new String(bytes, StandardCharsets.UTF_8);
                chatMessageDto.setMessage(utf8Message);
                System.out.println("📝 인코딩 변환 후 메시지: " + utf8Message);
            }
            
            messageTemplate.convertAndSend("/topic/" + chatMessageDto.getRoomId(), chatMessageDto);
            System.out.println("📢 WebSocket 클라이언트로 메시지 전송: /topic/" + chatMessageDto.getRoomId());
        } catch (JsonProcessingException e) {
            System.err.println("❌ JSON 파싱 오류: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
