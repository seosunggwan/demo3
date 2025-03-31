package com.example.backend.item;

import com.example.backend.item.domain.Item;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

    /**
     * 페이지네이션을 적용한 상품 목록 조회
     * @param offset 시작 위치
     * @param limit 가져올 데이터 수
     * @return 상품 목록
     */
    public List<Item> findAllWithPaging(int offset, int limit) {
        return em.createQuery("select i from Item i order by i.id desc", Item.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * 검색어로 상품 목록 조회
     * @param keyword 검색어
     * @param offset 시작 위치
     * @param limit 가져올 데이터 수
     * @return 검색된 상품 목록
     */
    public List<Item> findByNameContaining(String keyword, int offset, int limit) {
        return em.createQuery("select i from Item i where lower(i.name) like lower(:keyword) order by i.id desc", Item.class)
                .setParameter("keyword", "%" + keyword + "%")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * 검색어에 해당하는 상품 수 조회
     * @param keyword 검색어
     * @return 검색된 상품 수
     */
    public long countByNameContaining(String keyword) {
        return em.createQuery("select count(i) from Item i where lower(i.name) like lower(:keyword)", Long.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getSingleResult();
    }

    /**
     * 전체 상품 수 조회
     * @return 전체 상품 수
     */
    public long count() {
        return em.createQuery("select count(i) from Item i", Long.class)
                .getSingleResult();
    }

    @Transactional
    public void delete(Long id) {
        Item item = findOne(id);
        if (item == null) {
            throw new IllegalStateException("삭제할 상품을 찾을 수 없습니다.");
        }

        // OrderItem 참조 확인
        Long orderItemCount = em.createQuery(
                "select count(oi) from OrderItem oi where oi.item.id = :itemId", Long.class)
                .setParameter("itemId", id)
                .getSingleResult();
        
        if (orderItemCount > 0) {
            throw new IllegalStateException("이미 주문에 사용된 상품은 삭제할 수 없습니다.");
        }
        
        em.remove(item);
    }
}
