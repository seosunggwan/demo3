package com.example.backend.chat.repository;

import com.example.backend.chat.domain.ChatRoom;
import com.example.backend.chat.domain.ReadStatus;
import com.example.backend.security.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
    List<ReadStatus> findByChatRoomAndMember(ChatRoom chatRoom, UserEntity member);
    Long countByChatRoomAndMemberAndIsReadFalse(ChatRoom chatRoom, UserEntity member);
}
