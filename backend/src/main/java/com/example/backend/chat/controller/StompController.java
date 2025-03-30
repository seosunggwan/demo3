package com.example.backend.chat.controller;

import com.example.backend.chat.dto.ChatMessageDto;
import com.example.backend.chat.service.ChatService;
import com.example.backend.chat.service.RedisPubSubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.nio.charset.StandardCharsets;

@Controller
public class StompController {

    private final ChatService chatService;
    private final RedisPubSubService pubSubService;
    private final ObjectMapper objectMapper;

    public StompController(ChatService chatService, RedisPubSubService pubSubService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.pubSubService = pubSubService;
        this.objectMapper = objectMapper;
    }
////    방법1.MessageMapping(수신)과 SenTo(topic에 메시지전달)한꺼번에 처리
//    @MessageMapping("/{roomId}") //클라이언트에서 특정 publish/roomId형태로 메시지를 발행시 MessageMapping 수신
//    @SendTo("/topic/{roomId}")  //해당 roomId에 메시지를 발행하여 구독중인 클라이언트에게 메시지 전송
////    DestinationVariable : @MessageMapping 어노테이션으로 정의된 Websocket Controller 내에서만 사용
//    public String sendMessage(@DestinationVariable Long roomId, String message){
//        System.out.println(message);
//        return  message;
//    }

// //    방법2.MessageMapping어노테이션만 활용.
//     @MessageMapping("/{roomId}")
//     public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) throws JsonProcessingException {
//         System.out.println(chatMessageReqDto.getMessage());
//         chatService.saveMessage(roomId, chatMessageReqDto);
//         chatMessageReqDto.setRoomId(roomId);
// //        messageTemplate.convertAndSend("/topic/"+roomId, chatMessageReqDto);
//         ObjectMapper objectMapper = new ObjectMapper();
//         String message = objectMapper.writeValueAsString(chatMessageReqDto);
//         pubSubService.publish("chat", message);
//     }

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) throws JsonProcessingException {
        try {
            System.out.println("📩 메시지 수신: " + chatMessageReqDto);
            
            // 메시지 내용을 UTF-8로 명시적 변환하여 인코딩 문제 확인
            String originalMessage = chatMessageReqDto.getMessage();
            byte[] messageBytes = originalMessage != null ? originalMessage.getBytes(StandardCharsets.UTF_8) : new byte[0];
            String utf8Message = new String(messageBytes, StandardCharsets.UTF_8);
            System.out.println("🔤 원본 메시지: " + originalMessage);
            System.out.println("🔤 UTF-8 인코딩 메시지: " + utf8Message);
            
            // UTF-8로 인코딩된 메시지로 교체
            chatMessageReqDto.setMessage(utf8Message);

            if (roomId == null) {
                throw new IllegalArgumentException("🚨 roomId가 null입니다!");
            }
            if (chatMessageReqDto == null || chatMessageReqDto.getMessage() == null) {
                throw new IllegalArgumentException("🚨 메시지가 null입니다!");
            }

            // 이메일 형식이 올바른지 확인하고 필요한 경우 수정
            String email = chatMessageReqDto.getSenderEmail();
            if (email != null) {
                // oauth.user 접미사가 있는 경우 제거
                if (email.contains("@oauth.user")) {
                    email = email.replace("@oauth.user", "");
                    chatMessageReqDto.setSenderEmail(email);
                }
                
                System.out.println("🔄 메시지 발신자 이메일 확인: " + email);
            }

            chatService.saveMessage(roomId, chatMessageReqDto);
            chatMessageReqDto.setRoomId(roomId);

            // ObjectMapper에 UTF-8 설정 적용하여 인코딩 문제 해결
            objectMapper.getFactory().setCharacterEscapes(null);
            String message = objectMapper.writeValueAsString(chatMessageReqDto);
            System.out.println("📤 발송할 JSON 메시지: " + message);
            pubSubService.publish("chat", message);
        } catch (Exception e) {
            System.out.println("🔥 STOMP 메시지 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 자세한 오류 스택 추적 출력
        }
    }

}
