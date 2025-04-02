package com.example.backend.order;

import com.example.backend.delivery.Delivery;
import com.example.backend.delivery.DeliveryStatus;
import com.example.backend.item.ItemRepository;
import com.example.backend.item.domain.Item;
import com.example.backend.security.entity.UserEntity;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문 조회 (회원과 주문 상품 정보 포함)
     */
    public Order findOrderWithMemberAndItems(Long orderId) {
        return orderRepository.findOrderWithMemberAndItems(orderId);
    }

    /**
     * 주문 목록 조회 (회원과 주문 상품 정보 포함)
     */
    public List<Order> findOrdersWithMemberAndItems() {
        return orderRepository.findAllWithMemberAndItems();
    }

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        //엔티티 조회
        UserEntity member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();
    }

    //검색
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }

    /**
     * 페이지네이션이 적용된 주문 목록 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 주문 목록
     */
    public List<Order> findOrdersWithPaging(int page, int size) {
        int offset = page * size;
        return orderRepository.findAllWithPaging(offset, size);
    }

    /**
     * 페이지네이션이 적용된 주문 검색
     * @param orderSearch 검색 조건
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 주문 목록
     */
    public List<Order> findOrdersWithPaging(OrderSearch orderSearch, int page, int size) {
        int offset = page * size;
        return orderRepository.findAllByStringWithPaging(orderSearch, offset, size);
    }

    /**
     * 전체 주문 수 조회
     * @return 전체 주문 수
     */
    public long countOrders() {
        return orderRepository.count();
    }

    /**
     * 검색 조건에 맞는 주문 수 조회
     * @param orderSearch 검색 조건
     * @return 검색 결과 주문 수
     */
    public long countOrders(OrderSearch orderSearch) {
        return orderRepository.countBySearch(orderSearch);
    }
}
