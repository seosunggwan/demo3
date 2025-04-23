package com.example.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.backend.security.entity.UserEntity;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserDto {
    // UserEntity의 정보를 담는 DTO
    private Long id; // DB 저장되는 id : PK
    private String nickName; // 사용자 이름 (UserEntity의 username)
    private String email; // 사용자 이메일
    private String provider; // 소셜 제공자 정보 (현재 UserEntity에는 없음)

    /**
     * UserEntity를 ChatUserDto로 변환합니다.
     * 
     * @param userEntity 변환할 UserEntity 객체
     * @return 변환된 ChatUserDto 객체
     */
    public static ChatUserDto of(UserEntity userEntity) {
        ChatUserDto chatUserDto = ChatUserDto.builder()
                .id(userEntity.getId())
                .nickName(userEntity.getUsername())
                .email(userEntity.getEmail())
                // 현재 UserEntity에는 provider 정보가 없으므로, 필요에 따라 처리 방식 결정 필요
                .provider("default") // 기본값 설정 또는 비즈니스 로직에 맞게 수정
                .build();

        return chatUserDto;
    }
} 