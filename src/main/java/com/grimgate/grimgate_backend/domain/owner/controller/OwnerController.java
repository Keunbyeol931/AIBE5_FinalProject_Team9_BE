package com.grimgate.grimgate_backend.domain.owner.controller;

import com.grimgate.grimgate_backend.domain.owner.service.OwnerService;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeCreateRequest;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeResponse;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getOwnerThemes(){
        return ResponseEntity.ok(ownerService.getOwnerThemes());
    }

    @PostMapping("/themes")
    public ResponseEntity<Void> createTheme(
            @RequestBody @Valid ThemeCreateRequest request) {
        ownerService.createTheme(request);
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
