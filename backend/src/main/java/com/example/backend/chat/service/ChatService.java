package com.example.backend.chat.service;

import com.example.backend.chat.domain.ChatMessage;
import com.example.backend.chat.domain.ChatParticipant;
import com.example.backend.chat.domain.ChatRoom;
import com.example.backend.chat.domain.ReadStatus;
import com.example.backend.chat.dto.ChatMessageDto;
import com.example.backend.chat.dto.ChatRoomListResDto;
import com.example.backend.chat.dto.MyChatListResDto;
import com.example.backend.chat.repository.ChatMessageRepository;
import com.example.backend.chat.repository.ChatParticipantRepository;
import com.example.backend.chat.repository.ChatRoomRepository;
import com.example.backend.chat.repository.ReadStatusRepository;
import com.example.backend.securitylogin.entity.UserEntity;
import com.example.backend.securitylogin.repository.UserRepository;
import com.example.backend.securitylogin.service.oauth2.OAuthUserEntityToUserEntityService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@Transactional
public class ChatService {
    private static final Logger logger = Logger.getLogger(ChatService.class.getName());
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository memberRepository;
    private final OAuthUserEntityToUserEntityService oAuthUserService;
    
    public ChatService(
            ChatRoomRepository chatRoomRepository, 
            ChatParticipantRepository chatParticipantRepository, 
            ChatMessageRepository chatMessageRepository, 
            ReadStatusRepository readStatusRepository, 
            UserRepository memberRepository,
            OAuthUserEntityToUserEntityService oAuthUserService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.memberRepository = memberRepository;
        this.oAuthUserService = oAuthUserService;
    }

    public void saveMessage(Long roomId, ChatMessageDto chatMessageReqDto){
//        채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));

//        보낸사람조회
        String senderEmail = chatMessageReqDto.getSenderEmail();
        // 이메일 형식 처리 - 점(.)이 공백으로 대체된 경우도 처리
        UserEntity sender = null;
        try {
            sender = memberRepository.findByEmail(senderEmail)
                    .orElseThrow(() -> new EntityNotFoundException("member cannot be found with email: " + senderEmail));
        } catch (EntityNotFoundException e) {
            // 점(.)을 공백으로 치환해서 다시 시도
            if (senderEmail.contains(" ")) {
                String altEmail = senderEmail.replace(" ", ".");
                logger.info("대체 이메일 형식으로 검색 시도: " + altEmail);
                sender = memberRepository.findByEmail(altEmail)
                        .orElseThrow(() -> new EntityNotFoundException("member cannot be found with alternative email: " + altEmail));
            } else {
                throw e;
            }
        }

//        메시지저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageReqDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);
//        사용자별로 읽음여부 저장
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for(ChatParticipant c : chatParticipants){
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getMember().equals(sender))
                    .build();
            readStatusRepository.save(readStatus);
        }
    }

    public void createGroupRoom(String chatRoomName){
        // 현재 인증된 사용자의 UserEntity 가져오기 - 개선된 서비스 사용
        UserEntity member = oAuthUserService.getCurrentUserEntityFromOAuth();
        logger.info("채팅방 생성 사용자: " + member.getEmail());

//        채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomName)
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);
//        채팅참여자로 개설자를 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatRoomListResDto> getGroupchatRooms(){
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos = new ArrayList<>();
        for(ChatRoom c : chatRooms){
            ChatRoomListResDto dto = ChatRoomListResDto
                    .builder()
                    .roomId(c.getId())
                    .roomName(c.getName())
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }

    // 페이지네이션을 지원하는 그룹 채팅방 목록 조회 메소드
    public Map<String, Object> getGroupchatRooms(int page, int size) {
        // 페이지네이션 객체 생성
        Pageable pageable = PageRequest.of(page, size);
        
        // 페이지 단위로 그룹 채팅방 조회
        Page<ChatRoom> chatRoomsPage = chatRoomRepository.findByIsGroupChat("Y", pageable);
        
        // DTO로 변환
        List<ChatRoomListResDto> dtos = new ArrayList<>();
        for (ChatRoom c : chatRoomsPage.getContent()) {
            ChatRoomListResDto dto = ChatRoomListResDto
                    .builder()
                    .roomId(c.getId())
                    .roomName(c.getName())
                    .build();
            dtos.add(dto);
        }
        
        // 페이지 정보
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("page", page);
        pageInfo.put("size", size);
        pageInfo.put("total", chatRoomsPage.getTotalElements());
        pageInfo.put("totalPages", chatRoomsPage.getTotalPages());
        pageInfo.put("hasNext", chatRoomsPage.hasNext());
        pageInfo.put("hasPrevious", chatRoomsPage.hasPrevious());
        
        // 결과 맵
        Map<String, Object> result = new HashMap<>();
        result.put("rooms", dtos);
        result.put("pageInfo", pageInfo);
        
        return result;
    }

    public void addParticipantToGroupChat(Long roomId){
        logger.info("=== 그룹 채팅방 참여 시작 ===");
        logger.info("채팅방 ID: " + roomId);
        
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));
        logger.info("채팅방 조회 성공: " + chatRoom.getName());
        
        // 현재 인증된 사용자의 UserEntity 가져오기 - 개선된 서비스 사용
        try {
            // OAuthUserEntityToUserEntityService 사용하여 UserEntity 가져오기
            UserEntity member = oAuthUserService.getCurrentUserEntityFromOAuth();
            logger.info("사용자 정보 조회 성공: " + member.getEmail());
            
            if(chatRoom.getIsGroupChat().equals("N")){
                logger.warning("그룹 채팅방이 아닙니다: " + chatRoom.getId());
                throw new IllegalArgumentException("그룹채팅이 아닙니다.");
            }
            
            // 이미 참여자인지 검증
            Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);
            if(!participant.isPresent()){
                logger.info("새로운 참여자 추가: " + member.getEmail());
                addParticipantToRoom(chatRoom, member);
            } else {
                logger.info("이미 참여 중인 사용자: " + member.getEmail());
            }
            
        } catch (Exception e) {
            logger.severe("사용자 정보 처리 오류: " + e.getMessage());
            throw new EntityNotFoundException("채팅방 참여 중 사용자 정보를 찾을 수 없습니다: " + e.getMessage());
        }
        
        logger.info("=== 그룹 채팅방 참여 완료 ===");
    }
//        ChatParticipant객체생성 후 저장
    public void addParticipantToRoom(ChatRoom chatRoom, UserEntity member){
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatMessageDto> getChatHistory(Long roomId){
//        내가 해당 채팅방의 참여자가 아닐경우 에러
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        
        // 현재 인증된 사용자의 UserEntity 가져오기 - 개선된 서비스 사용
        UserEntity member = oAuthUserService.getCurrentUserEntityFromOAuth();
        logger.info("채팅 내역 조회 사용자: " + member.getEmail());
        
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        for(ChatParticipant c : chatParticipants){
            if(c.getMember().equals(member)){
                check = true;
            }
        }
        if(!check)throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");
//        특정 room에 대한 message조회
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for(ChatMessage c : chatMessages){
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .message(c.getContent())
                    .senderEmail(c.getMember().getEmail())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

    public boolean isRoomPaticipant(String email, Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        UserEntity member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member cannot be found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for(ChatParticipant c : chatParticipants){
            if(c.getMember().equals(member)){
                return true;
            }
        }
        return false;
    }

    public void messageRead(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        UserEntity member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("member cannot be found"));
        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndMember(chatRoom, member);
        for(ReadStatus r : readStatuses){
            r.updateIsRead(true);
        }
    }

    public List<MyChatListResDto> getMyChatRooms(){
        // 현재 인증된 사용자의 UserEntity 가져오기 - 개선된 서비스 사용
        UserEntity member = oAuthUserService.getCurrentUserEntityFromOAuth();
        logger.info("내 채팅방 조회 사용자: " + member.getEmail());
        
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(member);
        List<MyChatListResDto> chatListResDtos = new ArrayList<>();
        for(ChatParticipant c : chatParticipants){
            Long count = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(), member);
            MyChatListResDto dto = MyChatListResDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .unReadCount(count)
                    .build();
            chatListResDtos.add(dto);
        }
        return chatListResDtos;
    }

    public void leaveGroupChatRoom(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        // 현재 인증된 사용자의 UserEntity 가져오기 - 개선된 서비스 사용
        UserEntity member = oAuthUserService.getCurrentUserEntityFromOAuth();
        logger.info("채팅방 나가기 사용자: " + member.getEmail());
        
        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("단체 채팅방이 아닙니다.");
        }
        ChatParticipant c = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member).orElseThrow(()->new EntityNotFoundException("참여자를 찾을 수 없습니다."));
        chatParticipantRepository.delete(c);

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if(chatParticipants.isEmpty()){
            chatRoomRepository.delete(chatRoom);
        }
    }

    public Long getOrCreatePrivateRoom(Long otherMemberId){
        // 현재 인증된 사용자의 UserEntity 가져오기 - 개선된 서비스 사용
        UserEntity member = oAuthUserService.getCurrentUserEntityFromOAuth();
        logger.info("1:1 채팅방 생성/조회 사용자: " + member.getEmail());
        
        UserEntity otherMember = memberRepository.findById(otherMemberId).orElseThrow(()->new EntityNotFoundException("member cannot be found"));

//        나와 상대방이 1:1채팅에 이미 참석하고 있다면 해당 roomId return
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(member.getId(), otherMember.getId());
        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }
//        만약에 1:1채팅방이 없을경우 기존 채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat("N")
                .name(member.getUsername() + "-" + otherMember.getUsername())
                .build();
        chatRoomRepository.save(newRoom);
//        두사람 모두 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, member);
        addParticipantToRoom(newRoom, otherMember);

        return newRoom.getId();
    }
}

