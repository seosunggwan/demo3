package com.example.backend.order;

import com.example.backend.item.ItemService;
import com.example.backend.item.domain.Item;
import com.example.backend.order.dto.OrderFormData;
import com.example.backend.order.dto.OrderRequest;
import com.example.backend.order.dto.OrderResponseDto;
import com.example.backend.order.dto.PagedOrdersDto;
import com.example.backend.security.entity.UserEntity;
import com.example.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final ItemService itemService;

    @GetMapping("/order")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderFormData> createForm() {
        log.info("주문 폼 데이터 요청");
        List<UserEntity> members = userRepository.findAll();
        List<Item> items = itemService.findItems();

        OrderFormData formData = new OrderFormData(
            members.stream()
                .map(m -> new OrderFormData.MemberDto(m.getId(), m.getUsername()))
                .collect(Collectors.toList()),
            items.stream()
                .map(i -> new OrderFormData.ItemDto(i.getId(), i.getName(), i.getPrice(), i.getStockQuantity()))
                .collect(Collectors.toList())
        );
        
        log.info("조회된 회원 수: {}, 상품 수: {}", members.size(), items.size());
        return ResponseEntity.ok(formData);
    }

    @PostMapping("/orders")
    @Transactional
    public ResponseEntity<OrderResponseDto> order(@RequestBody OrderRequest request) {
        Long orderId = orderService.order(request.getMemberId(), request.getItemId(), request.getCount());
        Order order = orderService.findOrderWithMemberAndItems(orderId);
        return ResponseEntity.ok(new OrderResponseDto(order));
    }

    @GetMapping("/orders/{orderId}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        Order order = orderService.findOrderWithMemberAndItems(orderId);
        return ResponseEntity.ok(new OrderResponseDto(order));
    }

    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponseDto>> getOrders() {
        List<Order> orders = orderService.findOrdersWithMemberAndItems();
        List<OrderResponseDto> result = orders.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * 주문 검색 API
     * - 회원 이름, 주문 상태로 검색
     */
    @GetMapping("/orders/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponseDto>> searchOrders(
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) OrderStatus orderStatus) {
        
        log.info("주문 검색 요청 - 회원명: {}, 주문상태: {}", memberName, orderStatus);
        
        // 검색 조건 객체 생성
        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setMemberName(memberName);
        orderSearch.setOrderStatus(orderStatus);
        
        // 검색 실행
        List<Order> orders = orderService.findOrders(orderSearch);
        
        // 응답 DTO 변환
        List<OrderResponseDto> result = orders.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
        
        log.info("검색 결과 수: {}", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 페이지네이션이 적용된 주문 목록 조회 API
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 주문 목록과 페이지 정보
     */
    @GetMapping("/orders/page")
    @Transactional(readOnly = true)
    public ResponseEntity<PagedOrdersDto> getOrdersWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("페이지네이션 주문 목록 조회 요청 - 페이지: {}, 사이즈: {}", page, size);
        
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        // 서비스 호출
        List<Order> orders = orderService.findOrdersWithPaging(page, size);
        long total = orderService.countOrders();
        
        // 응답 DTO 생성
        PagedOrdersDto result = new PagedOrdersDto(orders, page, size, total);
        
        log.info("페이지 {}의 주문 {}개 조회 완료 (전체 {}개)", page, result.getOrders().size(), result.getPageInfo().getTotal());
        return ResponseEntity.ok(result);
    }

    /**
     * 페이지네이션이 적용된 주문 검색 API
     * @param memberName 회원 이름 검색어
     * @param orderStatus 주문 상태
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 주문 검색 결과와 페이지 정보
     */
    @GetMapping("/orders/search/page")
    @Transactional(readOnly = true)
    public ResponseEntity<PagedOrdersDto> searchOrdersWithPaging(
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("페이지네이션 주문 검색 요청 - 회원명: {}, 주문상태: {}, 페이지: {}, 사이즈: {}", 
                memberName, orderStatus, page, size);
        
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        // 검색 조건 객체 생성
        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setMemberName(memberName);
        orderSearch.setOrderStatus(orderStatus);
        
        // 서비스 호출
        List<Order> orders = orderService.findOrdersWithPaging(orderSearch, page, size);
        long total = orderService.countOrders(orderSearch);
        
        // 응답 DTO 생성
        PagedOrdersDto result = new PagedOrdersDto(orders, page, size, total);
        
        log.info("페이지 {}의 주문 {}개 검색 완료 (전체 {}개)", page, result.getOrders().size(), result.getPageInfo().getTotal());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/orders/{orderId}/cancel")
    @Transactional
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        Order order = orderService.findOrderWithMemberAndItems(orderId);
        return ResponseEntity.ok(new OrderResponseDto(order));
    }
}
