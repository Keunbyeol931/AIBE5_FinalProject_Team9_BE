package com.grimgate.grimgate_backend.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimgate.grimgate_backend.domain.ai.client.GeminiClient;
import com.grimgate.grimgate_backend.domain.ai.dto.AiRecommendRequest;
import com.grimgate.grimgate_backend.domain.ai.dto.AiRecommendResponse;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.theme.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendService {

    private final GeminiClient geminiClient;
    private final ThemeRepository themeRepository;
    private final ObjectMapper objectMapper;

    /**
     * AI-001: 대화 기반 테마 추천
     */
    public AiRecommendResponse recommend(AiRecommendRequest request) {
        // 마지막 user 메시지에서 키워드 추출
        String userMessage = extractLastUserMessage(request);

        // 키워드로 DB 1차 필터링 (tags 컬럼 기준)
        List<Theme> themes = themeRepository.findByTagsContaining(userMessage);
        if (themes.isEmpty()) {
            themes = themeRepository.findAll();
        }

        // 시스템 프롬프트 조합
        String systemPrompt = buildSystemPrompt(themes);

        // 대화 히스토리 변환 (assistant → model)
        List<Map<String, Object>> contents = request.messages().stream()
                .map(msg -> Map.<String, Object>of(
                        "role", msg.role().equals("assistant") ? "model" : msg.role(),
                        "parts", List.of(Map.of("text", msg.content()))
                ))
                .collect(Collectors.toList());

        try {
            String geminiResponse = geminiClient.call(contents, systemPrompt);
            return parseGeminiResponse(geminiResponse, themes);
        } catch (Exception e) {
            log.warn("Gemini 호출 실패, fallback 실행: {}", e.getMessage());
            return fallback(userMessage);
        }
    }

    /**
     * AI-002: 랜덤 테마 3개 추천 (Gemini 호출 없음)
     */
    public List<AiRecommendResponse.ThemeCard> random() {
        return themeRepository.findRandom(3)
                .stream()
                .map(AiRecommendResponse.ThemeCard::from)
                .toList();
    }

    // ── private 메서드 ──────────────────────────────────────────

    private String extractLastUserMessage(AiRecommendRequest request) {
        return request.messages().stream()
                .filter(m -> m.role() == AiRecommendRequest.Role.user)
                .reduce((first, second) -> second)
                .map(AiRecommendRequest.Message::content)
                .orElse("");
    }

    private String buildSystemPrompt(List<Theme> themes) {
        StringBuilder sb = new StringBuilder();
        sb.append("너는 방탈출 테마 추천 전문가야.\n");
        sb.append("아래는 현재 예약 가능한 테마 목록이야. theme_id를 기준으로 추천해줘.\n");
        sb.append("응답은 반드시 아래 두 형식 중 하나의 JSON만 반환해. 다른 텍스트는 절대 포함하지 마.\n\n");
        sb.append("추천할 경우: {\"type\": \"recommendation\", \"theme_ids\": [1, 3], \"message\": \"추천 이유\"}\n");
        sb.append("추천 불가 시: {\"type\": \"message\", \"message\": \"대화 응답\"}\n\n");
        sb.append("테마 목록:\n");

        for (Theme theme : themes) {
            // description 앞 50자만 전달
            String shortDesc = theme.getDescription().length() > 50
                    ? theme.getDescription().substring(0, 50) + "..."
                    : theme.getDescription();

            sb.append(String.format("%d | %s | %s | 난이도 %d | 공포도 %d | %s\n",
                    theme.getId(),
                    theme.getTitle(),
                    theme.getTags(),
                    theme.getDifficulty(),
                    theme.getHorrorLevel(),
                    shortDesc
            ));
        }

        return sb.toString();
    }

    private AiRecommendResponse parseGeminiResponse(String geminiText, List<Theme> allThemes) {
        try {
            // JSON 파싱
            String cleanJson = geminiText.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);

            String type = root.path("type").asText();
            String message = root.path("message").asText();

            if ("recommendation".equals(type)) {
                List<Long> themeIds = objectMapper.convertValue(
                        root.path("theme_ids"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class)
                );
                List<Theme> recommended = themeRepository.findAllById(themeIds);
                return AiRecommendResponse.recommend(message, recommended);
            } else {
                return AiRecommendResponse.message(message);
            }

        } catch (Exception e) {
            log.warn("Gemini 응답 파싱 실패, fallback 실행: {}", e.getMessage());
            return fallback("");
        }
    }

    private AiRecommendResponse fallback(String keyword) {
        List<Theme> themes = themeRepository.findByTagsContaining(keyword);
        if (themes.isEmpty()) {
            themes = themeRepository.findRandom(3);
        }
        return AiRecommendResponse.fallback(themes);
    }
}