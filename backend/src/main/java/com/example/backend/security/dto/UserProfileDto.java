package com.example.backend.security.dto;

import com.example.backend.security.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
    
    // 주소 정보
    private String city;
    private String street;
    private String zipcode;
    
    // 주소 객체로 변환하는 메소드
    public Address toAddress() {
        return Address.builder()
                .city(this.city)
                .street(this.street)
                .zipcode(this.zipcode)
                .build();
    }
    
    // 주소 정보를 설정하는 메소드
    public void setAddressInfo(Address address) {
        if (address != null) {
            this.city = address.getCity();
            this.street = address.getStreet();
            this.zipcode = address.getZipcode();
        }
    }
    
    // 프로필 이미지 URL 설정 메소드
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
} 