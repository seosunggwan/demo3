package com.example.backend.item;

import com.example.backend.item.domain.Book;
import com.example.backend.item.domain.Item;
import com.example.backend.item.dto.PagedItemsDto;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final AmazonS3 amazonS3;
    
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public Long saveItem(Item item) {
        itemRepository.save(item);
        return item.getId();
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    /**
     * 페이지네이션을 적용한 상품 목록 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이지네이션이 적용된 상품 목록과 페이지 정보
     */
    public PagedItemsDto findItemsWithPaging(int page, int size) {
        int offset = page * size;
        List<Item> items = itemRepository.findAllWithPaging(offset, size);
        long total = itemRepository.count();
        return new PagedItemsDto(items, page, size, total);
    }

    /**
     * 검색어로 상품 목록 조회 (페이지네이션 적용)
     * @param keyword 검색어
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 검색 결과 상품 목록과 페이지 정보
     */
    public PagedItemsDto searchItemsWithPaging(String keyword, int page, int size) {
        int offset = page * size;
        List<Item> items;
        long total;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            items = itemRepository.findByNameContaining(keyword.trim(), offset, size);
            total = itemRepository.countByNameContaining(keyword.trim());
        } else {
            items = itemRepository.findAllWithPaging(offset, size);
            total = itemRepository.count();
        }
        
        return new PagedItemsDto(items, page, size, total);
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

    @Transactional
    public void updateItem(Long id, String name, int price, int stockQuantity, String author, String isbn, String imageUrl) {
        Item item = itemRepository.findOne(id);
        if (item == null) {
            throw new IllegalStateException("수정할 상품을 찾을 수 없습니다.");
        }

        // 이미지 URL이 변경된 경우 기존 이미지 삭제
        String oldImageUrl = item.getImageUrl();
        if (oldImageUrl != null && !oldImageUrl.isEmpty() && !oldImageUrl.equals(imageUrl)) {
            log.info("이미지 URL 변경 감지 - 이전: {}, 새로운: {}", oldImageUrl, imageUrl);
            deleteImageFromS3(oldImageUrl);
        }

        item.setName(name);
        item.setPrice(price);
        item.setStockQuantity(stockQuantity);
        item.setImageUrl(imageUrl);

        // Book 엔티티인 경우 추가 필드 업데이트
        if (item instanceof Book) {
            Book book = (Book) item;
            if (author != null) {
                book.setAuthor(author);
            }
            if (isbn != null) {
                book.setIsbn(isbn);
            }
        }
    }

    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findOne(itemId);
        if (item != null && item.getImageUrl() != null) {
            // 이미지 URL이 있으면 S3에서 이미지도 삭제
            deleteImageFromS3(item.getImageUrl());
        }
        itemRepository.delete(itemId);
    }

    /**
     * S3에서 이미지를 삭제합니다.
     * 
     * @param imageUrl 삭제할 이미지의 URL
     */
    public void deleteImageFromS3(String imageUrl) {
        try {
            // URL에서 파일 키(경로) 추출
            String fileKey = extractFileKeyFromUrl(imageUrl);
            if (fileKey == null) {
                log.warn("이미지 URL에서 파일 키를 추출할 수 없습니다: {}", imageUrl);
                return;
            }
            
            log.info("S3에서 이미지 삭제 시도 - 버킷: {}, 키: {}", bucketName, fileKey);
            amazonS3.deleteObject(bucketName, fileKey);
            log.info("S3에서 이미지 삭제 완료 - 키: {}", fileKey);
        } catch (Exception e) {
            log.error("S3에서 이미지 삭제 중 오류 발생 - URL: {}, 오류: {}", imageUrl, e.getMessage(), e);
            // 이미지 삭제 실패해도 상품 삭제는 계속 진행
        }
    }

    /**
     * 이미지 URL에서 S3 파일 키(경로)를 추출합니다.
     * 
     * @param imageUrl 이미지 URL
     * @return 파일 키(경로)
     */
    private String extractFileKeyFromUrl(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return null;
            }
            
            // S3 URL 형식 확인 (https://버킷명.s3.리전.amazonaws.com/파일키)
            if (imageUrl.contains(bucketName + ".s3.")) {
                // URL에서 버킷명 이후의 경로 추출
                String[] parts = imageUrl.split(bucketName + ".s3.[^/]+/");
                if (parts.length > 1) {
                    return parts[1];
                }
            }
            
            // URL에 쿼리 파라미터가 있는 경우 제거
            if (imageUrl.contains("?")) {
                imageUrl = imageUrl.substring(0, imageUrl.indexOf("?"));
            }
            
            // 마지막 슬래시 이후의 문자열을 파일명으로 간주
            // 예: images/uuid-filename.jpg
            int lastSlashIndex = imageUrl.lastIndexOf("/");
            if (lastSlashIndex != -1 && lastSlashIndex < imageUrl.length() - 1) {
                String fileName = imageUrl.substring(lastSlashIndex + 1);
                return "images/" + fileName;
            }
            
            return null;
        } catch (Exception e) {
            log.error("이미지 URL에서 파일 키 추출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * S3 버킷에 업로드할 파일에 대한 Presigned URL을 생성합니다.
     * 
     * @param fileKey S3에 저장될 파일 키(경로)
     * @return Presigned URL 문자열
     */
    public String generatePresignedUrl(String fileKey) {
        try {
            // 파일 확장자로부터 Content-Type 결정
            String contentType = determineContentType(fileKey);
            
            // 10분 후 만료
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += TimeUnit.MINUTES.toMillis(10);
            expiration.setTime(expTimeMillis);
            
            // Presigned URL 생성 요청
            GeneratePresignedUrlRequest generatePresignedUrlRequest = 
                    new GeneratePresignedUrlRequest(bucketName, fileKey)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);
            
            // URL 쿼리 파라미터에 Content-Type 추가 (Presigned URL에 포함)
            // 이렇게 하면 클라이언트에서 Content-Type 헤더를 설정하지 않아도 됨
            generatePresignedUrlRequest.addRequestParameter(
                "Content-Type", 
                contentType
            );
            
            // Presigned URL 생성
            URL presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            log.info("Presigned URL 생성됨 - 키: {}, Content-Type: {}, URL: {}", fileKey, contentType, presignedUrl);
            
            return presignedUrl.toString();
        } catch (Exception e) {
            log.error("Presigned URL 생성 중 오류 발생 - 키: {}, 오류: {}", fileKey, e.getMessage(), e);
            throw new RuntimeException("Presigned URL 생성에 실패했습니다", e);
        }
    }
    
    /**
     * 파일 확장자에 따라 적절한 Content-Type을 결정합니다.
     * 
     * @param fileKey 파일 키(경로)
     * @return 파일 타입에 맞는 Content-Type
     */
    private String determineContentType(String fileKey) {
        String lowercaseKey = fileKey.toLowerCase();
        
        if (lowercaseKey.endsWith(".jpg") || lowercaseKey.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseKey.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseKey.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercaseKey.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowercaseKey.endsWith(".webp")) {
            return "image/webp";
        } else if (lowercaseKey.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            // 기본값
            return "application/octet-stream";
        }
    }
    
    /**
     * 파일 URL을 반환합니다.
     * 
     * @param fileKey S3에 저장된 파일 키(경로)
     * @return S3에 저장된 파일의 최종 URL
     */
    public String getFileUrl(String fileKey) {
        return amazonS3.getUrl(bucketName, fileKey).toString();
    }
}
