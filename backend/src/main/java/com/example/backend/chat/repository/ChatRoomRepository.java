package com.example.backend.chat.repository;

import com.example.backend.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByIsGroupChat(String isGroupChat);
    
    Page<ChatRoom> findByIsGroupChat(String isGroupChat, Pageable pageable);
    
    // 그룹 채팅방 중 이름으로 검색
    Page<ChatRoom> findByIsGroupChatAndNameContainingIgnoreCase(String isGroupChat, String keyword, Pageable pageable);
}
