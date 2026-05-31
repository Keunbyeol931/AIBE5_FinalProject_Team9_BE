package com.grimgate.grimgate_backend.domain.theme.dto;

import com.grimgate.grimgate_backend.global.response.TabCommonResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class BranchDetailResponse extends TabCommonResponse {
    private String branchName;
    private String region;
    private String operatingHours;
    private String phone;
    private String address;

    public BranchDetailResponse(Double rating, Integer reviewCount, Integer playTime,
                                Integer minPeople, Integer maxPeople, String thumbnailUrl,
                                String branchName, String region, String operatingHours,
                                String phone, String address) {
        super(rating, reviewCount, playTime, minPeople, maxPeople, thumbnailUrl);
        this.branchName = branchName;
        this.region = region;
        this.operatingHours = operatingHours;
        this.phone = phone;
        this.address = address;
    }

}
