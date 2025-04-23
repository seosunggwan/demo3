package com.example.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ChatMessageDto {
    // 메시지  타입 : 입장, 채팅
    // 메시지 타입에 따라서 동작하는 구조가 달라진다.
    // 입장과 퇴장 ENTER 과 LEAVE 의 경우 입장/퇴장 이벤트 처리가 실행되고,
    // TALK 는 말 그대로 내용이 해당 채팅방을 SUB 하고 있는 모든 클라이언트에게 전달된다.
    public enum MessageType{
        ENTER, TALK, LEAVE;
    }

    private MessageType type; // 메시지 타입
    private Long roomId; // 방 번호
    private String senderEmail; // 메시지 보낸사람 이메일
    private String message; // 메시지
    private String updateTime; // 메시지 발송 시간

    /* 파일 업로드 관련 변수 */
    private String s3DataUrl; // 파일 링크
    private String fileName; // 파일 이름
    private String fileDir; // S3 파일 경로
    private MultipartFile file; // 파일
}
