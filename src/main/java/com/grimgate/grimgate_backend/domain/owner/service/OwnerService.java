package com.grimgate.grimgate_backend.domain.owner.service;

import com.grimgate.grimgate_backend.domain.theme.dto.ThemeCreateRequest;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeResponse;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeUpdateRequest;
import com.grimgate.grimgate_backend.domain.theme.entity.Branch;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.theme.repository.BranchRepository;
import com.grimgate.grimgate_backend.domain.theme.repository.ThemeRepository;

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


    // 사장님 테마 관리 목록
    public List<ThemeResponse> getOwnerThemes(Long branchId) {
        return themeRepository.findByBranchId(branchId)
                .stream()
                .map(ThemeResponse::from)
                .collect(Collectors.toList());
    }

    //테마 등록
    public void createTheme(Long branchId, ThemeCreateRequest request) {
        Branch branch = branchRepository.getReferenceById(branchId);

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
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow();
        theme.update(request);
    }
    //테마 삭제
    public void deleteTheme(Long themeId) {
        themeRepository.deleteById(themeId);
    }
}
