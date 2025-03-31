package com.example.backend.item;

import com.example.backend.item.domain.Book;
import com.example.backend.item.domain.Item;
import com.example.backend.item.dto.ItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 상품 관리를 위한 REST API 컨트롤러
 * 상품의 조회, 등록, 수정, 삭제 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * 전체 상품 목록을 조회
     * @return 상품 목록
     */
    @GetMapping
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ItemDto>> list() {
        log.info("상품 목록 조회 요청");
        List<Item> items = itemService.findItems();
        List<ItemDto> itemDtos = items.stream()
            .map(item -> ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity())
                .imageUrl(item.getImageUrl())
                .build())
            .collect(Collectors.toList());
        log.info("조회된 상품 수: {}", items.size());
        return ResponseEntity.ok(itemDtos);
    }

    /**
     * 페이지네이션이 적용된 상품 목록을 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 상품 목록과 페이지 정보
     */
    @GetMapping("/page")
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("페이지네이션 상품 목록 조회 요청 - 페이지: {}, 사이즈: {}", page, size);
        
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        // 서비스 호출
        com.example.backend.item.dto.PagedItemsDto result = itemService.findItemsWithPaging(page, size);
        
        log.info("페이지 {}의 상품 {}개 조회 완료 (전체 {}개)", page, result.getItems().size(), result.getPageInfo().getTotal());
        return ResponseEntity.ok(result);
    }

    /**
     * 상품 이름으로 검색 (페이지네이션 적용)
     * @param keyword 검색어
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 검색 결과 상품 목록과 페이지 정보
     */
    @GetMapping("/search")
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchWithPaging(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("상품 검색 요청 - 검색어: '{}', 페이지: {}, 사이즈: {}", keyword, page, size);
        
        // 페이지 크기 제한
        if (size > 50) {
            size = 50;
        }
        
        // 서비스 호출
        com.example.backend.item.dto.PagedItemsDto result = itemService.searchItemsWithPaging(keyword, page, size);
        
        log.info("검색 결과: 페이지 {}의 상품 {}개 조회 완료 (전체 {}개)", 
                page, result.getItems().size(), result.getPageInfo().getTotal());
        return ResponseEntity.ok(result);
    }

    /**
     * 새로운 상품 등록을 위한 폼 데이터를 반환
     * @return 빈 BookForm 객체
     */
    @GetMapping("/new")
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookForm> createForm() {
        return ResponseEntity.ok(new BookForm());
    }

    /**
     * 새로운 상품을 등록
     * @param form 등록할 상품 정보
     * @return 생성된 상품의 ID
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> create(@RequestBody BookForm form) {
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        book.setImageUrl(form.getImageUrl());

        Long id = itemService.saveItem(book);
        return ResponseEntity.ok(id);
    }

    /**
     * 상품 수정을 위한 폼 데이터를 반환
     * @param itemId 수정할 상품의 ID
     * @return 상품 정보가 담긴 BookForm 객체
     */
    @GetMapping("/{itemId}/edit")
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookForm> updateItemForm(@PathVariable("itemId") Long itemId) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());
        form.setImageUrl(item.getImageUrl());

        return ResponseEntity.ok(form);
    }

    /**
     * 상품 정보를 수정
     * @param itemId 수정할 상품의 ID
     * @param form 수정할 상품 정보
     * @return 수정 완료 응답
     */
    @PutMapping("/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateItem(@PathVariable Long itemId, @RequestBody BookForm form) {
        log.info("상품 수정 요청 - 상품 ID: {}, 수정 정보: {}", itemId, form);
        try {
            itemService.updateItem(
                itemId,
                form.getName(),
                form.getPrice(),
                form.getStockQuantity(),
                form.getAuthor(),
                form.getIsbn(),
                form.getImageUrl()
            );
            log.info("상품 수정 완료 - 상품 ID: {}", itemId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.warn("상품 수정 실패 - 상품 ID: {}, 사유: {}", itemId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("상품 수정 중 오류 발생 - 상품 ID: {}, 오류: {}", itemId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    

    /**
     * 상품을 삭제
     * @param itemId 삭제할 상품의 ID
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{itemId}")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId) {
        log.info("상품 삭제 요청 - 상품 ID: {}", itemId);
        try {
            // 조회 부분을 별도의 트랜잭션으로 처리
            Item item = itemService.findOne(itemId);
            if (item == null) {
                log.warn("삭제할 상품을 찾을 수 없음 - 상품 ID: {}", itemId);
                return ResponseEntity.notFound().build();
            }
            
            // 삭제 작업 수행
            itemService.deleteItem(itemId);
            log.info("상품 삭제 완료 - 상품 ID: {}", itemId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.warn("상품 삭제 실패 - 상품 ID: {}, 사유: {}", itemId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생 - 상품 ID: {}, 오류: {}", itemId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "상품 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * Presigned URL 요청을 위한 레코드
     */
    public record PresignedUrlRequest(String filename) {}
    
    /**
     * S3 이미지 업로드를 위한 Presigned URL 생성 엔드포인트
     * @param request 파일명을 포함한 요청 객체
     * @return Presigned URL과 최종 파일 URL
     */
    @PostMapping("/image/presigned")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestBody PresignedUrlRequest request) {
        log.info("Presigned URL 요청 - 파일명: {}", request.filename());
        try {
            // 파일명으로부터 고유 키 생성 (UUID)
            String originalFilename = request.filename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileKey = "images/" + UUID.randomUUID() + fileExtension;
            
            // S3 Presigned URL 생성 로직
            final String presignedUrl = itemService.generatePresignedUrl(fileKey);
            final String fileUrl = itemService.getFileUrl(fileKey);
            
            log.info("Presigned URL 생성 완료 - 파일명: {}, 키: {}", originalFilename, fileKey);
            return ResponseEntity.ok(Map.of(
                "presignedUrl", presignedUrl,
                "fileUrl", fileUrl
            ));
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패 - 파일명: {}, 오류: {}", request.filename(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "이미지 업로드 URL 생성에 실패했습니다."));
        }
    }

    /**
     * 이미지 삭제 요청을 위한 레코드
     */
    public record DeleteImageRequest(String imageUrl) {}

    /**
     * S3에서 이미지를 삭제
     * @param request 삭제할 이미지 URL이 포함된 요청 객체
     * @return 삭제 결과
     */
    @DeleteMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteImage(@RequestBody DeleteImageRequest request) {
        log.info("이미지 삭제 요청 - URL: {}", request.imageUrl());
        try {
            // 이미지 URL 유효성 검사
            if (request.imageUrl() == null || request.imageUrl().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "삭제할 이미지 URL이 제공되지 않았습니다."));
            }
            
            // S3에서 이미지 삭제
            itemService.deleteImageFromS3(request.imageUrl());
            
            log.info("이미지 삭제 완료 - URL: {}", request.imageUrl());
            return ResponseEntity.ok(Map.of("message", "이미지가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생 - URL: {}, 오류: {}", request.imageUrl(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "이미지 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}