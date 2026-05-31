package com.grimgate.grimgate_backend.domain.theme.dto;

import com.grimgate.grimgate_backend.global.response.TabCommonResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

//테마 상세페이지 전용 응답 DTO
@Getter
public class ThemeDetailResponse extends TabCommonResponse {
    //상세정보 탭
    private String branchCode;
    private String branchName;
    private String region;
    private String address;
    private String phone;
    private String operatingHours;
    private String description;

    public ThemeDetailResponse(Double rating, Integer reviewCount, Integer playTime,
                               Integer minPeople, Integer maxPeople, String thumbnailUrl,
                               String branchCode, String branchName, String region,
                               String address, String phone, String operatingHours, String description) {
        super(rating, reviewCount, playTime, minPeople, maxPeople, thumbnailUrl);
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.region = region;
        this.address = address;
        this.phone = phone;
        this.operatingHours = operatingHours;
        this.description = description;
    }


}
