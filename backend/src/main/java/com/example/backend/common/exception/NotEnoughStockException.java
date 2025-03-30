package com.example.backend.common.exception;

/**
 * 상품의 재고가 부족할 때 발생하는 예외 클래스
 * 주문 시 요청한 수량이 현재 재고 수량보다 많을 경우 발생
 */
public class NotEnoughStockException extends RuntimeException {
    /**
     * 기본 생성자
     * 기본 에러 메시지 "재고가 부족합니다."를 설정
     */
    public NotEnoughStockException() {
        super("재고가 부족합니다.");
    }

    /**
     * 메시지를 받아서 예외를 생성하는 생성자
     * @param message 예외 발생 시 표시할 에러 메시지
     */
    public NotEnoughStockException(String message) {
        super(message);
    }

    /**
     * 원인 예외를 포함하여 예외를 생성하는 생성자
     * @param message 예외 발생 시 표시할 에러 메시지
     * @param cause 원인이 되는 예외
     */
    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인 예외만 받아서 예외를 생성하는 생성자
     * @param cause 원인이 되는 예외
     */
    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }
} 