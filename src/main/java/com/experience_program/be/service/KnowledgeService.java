package com.experience_program.be.service;

import com.experience_program.be.dto.AiKnowledgeDto;
import com.experience_program.be.dto.KnowledgeRequestDto;
import com.experience_program.be.dto.KnowledgeUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class KnowledgeService {

    private final WebClient webClient;

    @Autowired
    public KnowledgeService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void registerKnowledge(KnowledgeRequestDto requestDto) {
        // 서버에서 고유 ID와 현재 시간을 생성
        String newCampaignId = "knowledge-" + UUID.randomUUID().toString();
        LocalDateTime registrationDateTime = LocalDateTime.now();

        // AI 서버로 보낼 전용 DTO를 생성합니다.
        AiKnowledgeDto knowledgeData = new AiKnowledgeDto(
                newCampaignId,
                requestDto.getCampaignSummary(),
                requestDto.getCampaignDetails(),
                registrationDateTime
        );

        webClient.post()
                .uri("/api/knowledge")
                .body(Mono.just(knowledgeData), AiKnowledgeDto.class)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> {
                    // 에러 로깅 (실제 프로덕션에서는 더 정교한 로깅 필요)
                    System.err.println("Error while registering knowledge: " + error.getMessage());
                })
                .subscribe();
    }

    public Mono<Object> getAllKnowledge() {
        return webClient.get()
                .uri("/api/knowledge")
                .retrieve()
                .bodyToMono(Object.class);
    }

    public Mono<Object> getKnowledgeById(String knowledgeId) {
        return webClient.get()
                .uri("/api/knowledge/" + knowledgeId)
                .retrieve()
                .bodyToMono(Object.class);
    }

    public void updateKnowledge(String knowledgeId, KnowledgeUpdateDto requestDto) {
        // AI 서버의 명세에 맞는 중첩된 Map 구조를 생성합니다.
        Map<String, Object> metadata = new HashMap<>(requestDto.getCampaignDetails());
        metadata.put("campaign_id", knowledgeId);
        metadata.put("status", "successful"); // 또는 다른 적절한 상태값
        metadata.put("registration_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("document", requestDto.getCampaignSummary());
        requestBody.put("metadata", metadata);

        webClient.put()
                .uri("/api/knowledge/" + knowledgeId)
                .body(Mono.just(requestBody), Map.class)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> {
                    // 에러 로깅
                    System.err.println("Error while updating knowledge: " + error.getMessage());
                })
                .subscribe();
    }

    public void deleteKnowledge(String knowledgeId) {
        webClient.delete()
                .uri("/api/knowledge/" + knowledgeId)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> {
                    // 에러 로깅
                    System.err.println("Error while deleting knowledge: " + error.getMessage());
                })
                .subscribe();
    }
}
