package com.example.backend.order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class OrderRepository {

    private final EntityManager em;

    public OrderRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * 주문 조회 (회원과 주문 상품 정보를 함께 조회)
     */
    public Order findOrderWithMemberAndItems(Long orderId) {
        return em.createQuery(
                "select o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d " +
                        "join fetch o.orderItems oi " +
                        "join fetch oi.item i " +
                        "where o.id = :orderId", Order.class)
                .setParameter("orderId", orderId)
                .getSingleResult();
    }

    /**
     * 주문 목록 조회 (회원과 주문 상품 정보를 함께 조회)
     */
    public List<Order> findAllWithMemberAndItems() {
        return em.createQuery(
                "select distinct o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d " +
                        "join fetch o.orderItems oi " +
                        "join fetch oi.item i", Order.class)
                .getResultList();
    }

    public List<Order> findAll() {
        return em.createQuery("select o from Order o", Order.class)
                .getResultList();
    }

    /**
     * 페이지네이션을 적용한 주문 목록 조회
     * @param offset 시작 위치
     * @param limit 가져올 데이터 수
     * @return 주문 목록
     */
    public List<Order> findAllWithPaging(int offset, int limit) {
        return em.createQuery(
            "select distinct o from Order o " +
            "join fetch o.member m " +
            "join fetch o.delivery d " +
            "join fetch o.orderItems oi " +
            "join fetch oi.item i " +
            "order by o.orderDate desc", Order.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    /**
     * 페이지네이션을 적용한 주문 검색
     * @param orderSearch 검색 조건
     * @param offset 시작 위치
     * @param limit 가져올 데이터 수
     * @return 검색 결과 주문 목록
     */
    public List<Order> findAllByStringWithPaging(OrderSearch orderSearch, int offset, int limit) {
        String jpql = "select distinct o from Order o " +
                      "join fetch o.member m " +
                      "join fetch o.delivery d " +
                      "join fetch o.orderItems oi " +
                      "join fetch oi.item i";
        
        boolean isFirstCondition = true;
        
        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        
        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.username like :name";
        }
        
        // 정렬 조건 추가
        jpql += " order by o.orderDate desc";
        
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
            .setFirstResult(offset)
            .setMaxResults(limit);
        
        if (orderSearch.getOrderStatus() != null) {
            query.setParameter("status", orderSearch.getOrderStatus());
        }
        
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query.setParameter("name", "%" + orderSearch.getMemberName() + "%");
        }
        
        return query.getResultList();
    }

    /**
     * 전체 주문 수 조회
     * @return 전체 주문 수
     */
    public long count() {
        return em.createQuery("select count(o) from Order o", Long.class)
            .getSingleResult();
    }

    /**
     * 검색 조건에 맞는 주문 수 조회
     * @param orderSearch 검색 조건
     * @return 검색 결과 주문 수
     */
    public long countBySearch(OrderSearch orderSearch) {
        String jpql = "select count(o) from Order o join o.member m";
        
        boolean isFirstCondition = true;
        
        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        
        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.username like :name";
        }
        
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        
        if (orderSearch.getOrderStatus() != null) {
            query.setParameter("status", orderSearch.getOrderStatus());
        }
        
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query.setParameter("name", "%" + orderSearch.getMemberName() + "%");
        }
        
        return query.getSingleResult();
    }

    /**
     * JPQL로 검색 (문자열 동적 쿼리 방식)
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select distinct o from Order o " +
                      "join fetch o.member m " +
                      "join fetch o.delivery d " +
                      "join fetch o.orderItems oi " +
                      "join fetch oi.item i";
        
        boolean isFirstCondition = true;
        
        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        
        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.username like :name";
        }
        
        TypedQuery<Order> query = em.createQuery(jpql, Order.class);
        
        if (orderSearch.getOrderStatus() != null) {
            query.setParameter("status", orderSearch.getOrderStatus());
        }
        
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query.setParameter("name", "%" + orderSearch.getMemberName() + "%");
        }
        
        return query.getResultList();
    }
    
    /**
     * JPA Criteria로 검색 (자바 코드 기반 동적 쿼리 방식)
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        // Criteria API는 fetch join을 명시적으로 지원하지 않기 때문에
        // 복잡한 경우 JPQL을 사용하는 것이 더 좋습니다.
        // 이 메서드는 보관용으로 유지하고 실제로는 findAllByString 메서드를 사용합니다.
        return findAllByString(orderSearch);
    }
}

