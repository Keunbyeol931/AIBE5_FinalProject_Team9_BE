package com.grimgate.grimgate_backend.domain.owner.service;

import com.grimgate.grimgate_backend.domain.account.entity.Account;
import com.grimgate.grimgate_backend.domain.manager.entity.Manager;
import com.grimgate.grimgate_backend.domain.manager.repository.ManagerRepository;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeCreateRequest;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeUpdateRequest;
import com.grimgate.grimgate_backend.domain.theme.entity.Branch;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.theme.repository.BranchRepository;
import com.grimgate.grimgate_backend.domain.theme.repository.ThemeRepository;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class OwnerServiceTest {

    @InjectMocks
    private OwnerService ownerService;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Test
    @DisplayName("테마 등록 성공")

    void createTheme_success() {
        Account account = Account.builder()
                .id(1L)
                .build();

        Manager manager = Manager.builder()
                .id(1L)
                .account(account).build();

        Branch branch = Branch.builder()
                .id(1L)
                .build();

        ThemeCreateRequest request = new ThemeCreateRequest();

        when(managerRepository.findByAccount_Id(any())).thenReturn(Optional.of(manager));
        when(themeRepository.save(any())).thenReturn(Optional.of(branch));

        // when
        ownerService.createTheme(request);

        // then
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    @Test
    @DisplayName("테마 수정 성공")
    void updateTheme_success() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(1L);

            Manager manager = Manager.builder().id(1L).build();
            Branch branch = Branch.builder().id(1L).build();
            Theme theme = Theme.builder().branch(branch).build();
            ThemeUpdateRequest request = new ThemeUpdateRequest();

            when(managerRepository.findByAccount_Id(any())).thenReturn(Optional.of(manager));
            when(branchRepository.findByManagerId(any())).thenReturn(Optional.of(branch));
            when(themeRepository.findById(any())).thenReturn(Optional.of(theme));

            // when
            ownerService.updateTheme(1L, request);

            // then
            verify(themeRepository, times(1)).findById(1L);
        }
    }

    @Test
    @DisplayName("테마 삭제 성공")
    void deleteTheme_success() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(1L);

            Manager manager = Manager.builder().id(1L).build();
            Branch branch = Branch.builder().id(1L).build();
            Theme theme = Theme.builder().branch(branch).build();

            when(managerRepository.findByAccount_Id(any())).thenReturn(Optional.of(manager));
            when(branchRepository.findByManagerId(any())).thenReturn(Optional.of(branch));
            when(themeRepository.findById(any())).thenReturn(Optional.of(theme));

            // when
            ownerService.deleteTheme(1L);

            // then
            verify(themeRepository, times(1)).deleteById(1L);
        }
    }

    @Test
    @DisplayName("다른 지점 테마 수정, 삭제 실패")
    void updateTheme_forbidden() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(1L);

            Manager manager = Manager.builder().id(1L).build();
            Branch myBranch = Branch.builder().id(1L).build();      // 내 지점
            Branch otherBranch = Branch.builder().id(2L).build();   // 다른 지점
            Theme theme = Theme.builder().branch(otherBranch).build(); // 다른 지점 테마
            ThemeUpdateRequest request = new ThemeUpdateRequest();

            when(managerRepository.findByAccount_Id(any())).thenReturn(Optional.of(manager));
            when(branchRepository.findByManagerId(any())).thenReturn(Optional.of(myBranch));
            when(themeRepository.findById(any())).thenReturn(Optional.of(theme));

            // then - 예외 발생해야 함
            assertThrows(CustomException.class, () -> ownerService.updateTheme(1L, request));
            assertThrows(CustomException.class, () -> ownerService.deleteTheme(1L));
        }
    }


}