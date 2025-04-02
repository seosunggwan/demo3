package com.example.backend.common.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.lang.NonNull;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler;

    public StompWebSocketConfig(StompHandler stompHandler) {
        this.stompHandler = stompHandler;
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOrigins("http://localhost:5173")
//                ws://가 아닌 http:// 엔드포인트를 사용할수 있게 해주는 sockJs라이브러리를 통한 요청을 허용하는 설정.
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
//        /publish/1형태로 메시지 발행해야 함을 설정
//        /publish로 시작하는 url패턴으로 메시지가 발행되면 @Controller 객체의 @MessaMapping메서드로 라우팅
        registry.setApplicationDestinationPrefixes("/publish");

//        /topic/1형태로 메시지를 수신(subscribe)해야 함을 설정
        registry.enableSimpleBroker("/topic");

    }


//    웹소켓요청(connect, subscribe, disconnect)등의 요청시에는 http header등 http메시지를 넣어올수 있고, 이를 interceptor를 통해 가로채 토큰등을 검증할수 있음.
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }

    // 메시지 컨버터 설정 - UTF-8 인코딩 처리
    @Override
    public boolean configureMessageConverters(@NonNull List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        messageConverters.add(converter);
        return false; // false 반환하면 기본 컨버터도 유지됨
    }
}
