package com.grimgate.grimgate_backend.domain.owner.service;

import com.grimgate.grimgate_backend.domain.manager.entity.Manager;
import com.grimgate.grimgate_backend.domain.manager.repository.ManagerRepository;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeCreateRequest;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeResponse;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeUpdateRequest;
import com.grimgate.grimgate_backend.domain.theme.entity.Branch;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.theme.repository.BranchRepository;
import com.grimgate.grimgate_backend.domain.theme.repository.ThemeRepository;

import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final ThemeRepository themeRepository;
    private final BranchRepository branchRepository;
    private final ManagerRepository managerRepository;

    // 사장님 테마 관리 목록
    public List<ThemeResponse> getOwnerThemes(Long branchId) {
        return themeRepository.findByBranchId(branchId)
                .stream()
                .map(ThemeResponse::from)
                .collect(Collectors.toList());
    }

    //테마 등록
    public void createTheme( ThemeCreateRequest request) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        Manager manager = managerRepository.findByAccount_Id(accountId).orElseThrow();
        Branch branch = branchRepository.findByManagerId(manager.getId()).orElseThrow();

        Theme theme = Theme.builder()
                .branch(branch)
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .horrorLevel(request.getHorrorLevel())
                .ageLimit(request.getAgeLimit())
                .playTime(request.getPlayTime())
                .minPeople(request.getMinPeople())
                .maxPeople(request.getMaxPeople())
                .price(request.getPrice())
                .tags(request.getTags())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        themeRepository.save(theme);
    }

    //테마 수정
    @Transactional
    public void updateTheme(Long themeId, ThemeUpdateRequest request) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        Manager manager = managerRepository.findByAccount_Id(accountId).orElseThrow();
        Branch branch = branchRepository.findByManagerId(manager.getId()).orElseThrow();
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow();

        // 본인 지점 테마인지 검증
        if (!theme.getBranch().getId().equals(branch.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        theme.update(request);
    }

    //테마 삭제
    @Transactional
    public void deleteTheme(Long themeId) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        Manager manager = managerRepository.findByAccount_Id(accountId).orElseThrow();
        Branch branch = branchRepository.findByManagerId(manager.getId()).orElseThrow();

        Theme theme = themeRepository.findById(themeId).orElseThrow();

        // 본인 지점 테마인지 검증
        if (!theme.getBranch().getId().equals(branch.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        themeRepository.deleteById(themeId);
    }


    //테마 전체 조회
    public List<ThemeResponse> getOwnerThemes() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        Manager manager = managerRepository.findByAccount_Id(accountId)
                .orElseThrow();
        Branch branch = branchRepository.findByManagerId(manager.getId())
                .orElseThrow();
        return themeRepository.findByBranchId(branch.getId())
                .stream()
                .map(ThemeResponse::from)
                .collect(Collectors.toList());
    }


}
