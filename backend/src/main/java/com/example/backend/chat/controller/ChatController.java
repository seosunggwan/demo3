package com.example.backend.chat.controller;

import com.example.backend.chat.dto.ChatMessageDto;
import com.example.backend.chat.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

//    그룹채팅방 개설
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestParam String roomName){
        chatService.createGroupRoom(roomName);
        return ResponseEntity.ok().build();
    }

//    그룹채팅목록조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupChatRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        System.out.println("=== ChatController: 그룹 채팅방 목록 요청 ===");
        System.out.println("요청 파라미터 - page: " + page + ", size: " + size + ", keyword: '" + keyword + "'");
        
        Map<String, Object> result = chatService.getGroupchatRooms(page, size, keyword);
        System.out.println("응답 결과 - 채팅방 수: " + ((List<?>)result.get("rooms")).size());
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

//    그룹채팅방참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long roomId){
        chatService.addParticipantToGroupChat(roomId);
        return ResponseEntity.ok().build();
    }

//    이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId){
        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(roomId);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }

//    채팅메시지 읽음처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId){
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

//    내채팅방목록조회 : roomId, roomName, 그룹채팅여부, 메시지읽음개수
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        System.out.println("=== ChatController: 내 채팅방 목록 요청 ===");
        System.out.println("요청 파라미터 - 검색어: '" + keyword + "', 페이지: " + page + ", 크기: " + size);
        
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        // 페이지네이션 버전 호출
        Map<String, Object> result = chatService.searchMyChatRoomsWithPaging(keyword, page, size);
        System.out.println("응답 결과 - 채팅방 수: " + ((List<?>)result.get("rooms")).size());
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

//    채팅방 나가기
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long roomId){
        chatService.leaveGroupChatRoom(roomId);
        return ResponseEntity.ok().build();
    }

//    개인 채팅방 개설 또는 기존roomId return
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long otherMemberId){
        Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }
}
