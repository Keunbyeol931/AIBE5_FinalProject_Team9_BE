INSERT IGNORE INTO profile_character (id, name, image_url, type, created_at, updated_at)
VALUES (1, '기본 캐릭터', '/images/default.png', 'DEFAULT', NOW(), NOW());

-- 사이트 관리자 테스트 계정 (ADMIN)
-- 평문 비밀번호: Admin1234!
INSERT IGNORE INTO account (nickname, email, password, phone, role, notification_enabled, age_visible, gender_visible, email_visible, created_at, updated_at)
VALUES ('grimgate-admin', 'admin@grimgate.com', '$2a$10$cgKJRbRZrArZtYVAzTwl2OEDbzmuSgyUxUCaQdXKPq2D9SlRapysu', '01000000000', 'ADMIN', true, true, true, true, NOW(), NOW());
INSERT IGNORE INTO title (id, name, description, min_success_rate, max_success_rate, required_clear_count, created_at, updated_at)
VALUES
(1, '쫄보',           '성공률 30% 미만',                    0,  29, null, NOW(), NOW()),
(2, '일반인',         '성공률 30~50%',                      30, 49, null, NOW(), NOW()),
(3, '강심장',         '성공률 50~70%',                      50, 69, null, NOW(), NOW()),
(4, '오컬트동호회장', '성공률 70~85%',                      70, 84, null, NOW(), NOW()),
(5, '퇴마사',         '성공률 85% 이상 + 5회 이상 클리어',  85, 100, 5,  NOW(), NOW());
       
-- ============================================================
-- 미니게임 단계 시드 데이터
-- 기획서 기준 4단계 퍼즐
-- ============================================================
INSERT IGNORE INTO minigame_stage (id, stage_no, stage_type, answer, hint_text, description, created_at, updated_at)
VALUES
(1, 1, 'CALENDAR_DEC',    '2025-12-30', '예약된 날짜를 찾아보세요. 12월 달력을 잘 보세요.',     '12월 달력에서 예약된 날짜를 선택하세요.', NOW(), NOW()),
(2, 2, 'ENTRY_LOG',       '08501751',   '출입 기록에서 8자리 숫자를 찾아보세요.',                '출입 기록 퍼즐을 풀어 8자리 숫자를 입력하세요.', NOW(), NOW()),
(3, 3, 'EMPTY_SLOT',      '점심시간',     '비어있는 시간대는 식사 시간일 수 있어요.',              '비어있는 시간대가 무엇인지 답하세요.', NOW(), NOW()),
(4, 4, 'FINAL_CALENDAR',  '0702',       '마지막 단서를 모아 날짜 4자리를 입력하세요. (MMDD)',    '최종 달력에서 정답 날짜 4자리(MMDD)를 입력하세요.', NOW(), NOW());
