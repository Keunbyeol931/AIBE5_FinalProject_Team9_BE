package com.grimgate.grimgate_backend.domain.owner.controller;

import com.grimgate.grimgate_backend.domain.owner.service.OwnerService;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeCreateRequest;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeResponse;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeUpdateRequest;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.user.entity.Owner;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getOwnerThemes(
            // TODO: JWT 구현 후 @AuthenticationPrincipal로 교체
            @RequestParam Long branchId) {
        return ResponseEntity.ok(ownerService.getOwnerThemes(branchId));
    }

    @PostMapping("/themes")
    public ResponseEntity<Void> createTheme(
            // TODO: JWT 구현 후 @AuthenticationPrincipal로 교체
            @RequestParam Long branchId,
            @RequestBody @Valid ThemeCreateRequest request) {
        ownerService.createTheme(branchId, request);
        return ResponseEntity.ok().build();
    }

    //수정
    @PatchMapping("/themes/{themeId}")
    public ResponseEntity<Void> updateTheme(
            @PathVariable Long themeId,
            @RequestBody @Valid ThemeUpdateRequest request) {
        ownerService.updateTheme(themeId, request);
        return ResponseEntity.ok().build();
    }

    //삭제
    @DeleteMapping("/themes/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long themeId) {
        ownerService.deleteTheme(themeId);
        return ResponseEntity.ok().build();
    }
}
