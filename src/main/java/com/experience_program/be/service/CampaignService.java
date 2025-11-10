package com.experience_program.be.service;

import com.experience_program.be.controller.CampaignSpecification;
import com.experience_program.be.dto.*;
import com.experience_program.be.entity.Campaign;
import com.experience_program.be.entity.MessageResult;
import com.experience_program.be.repository.CampaignRepository;
import com.experience_program.be.repository.MessageResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final MessageResultRepository messageResultRepository;
    private final WebClient webClient;

    @Autowired
    public CampaignService(CampaignRepository campaignRepository, MessageResultRepository messageResultRepository, WebClient webClient) {
        this.campaignRepository = campaignRepository;
        this.messageResultRepository = messageResultRepository;
        this.webClient = webClient;
    }

    @Transactional
    public Campaign createCampaign(CampaignRequestDto campaignRequestDto) {
        Campaign campaign = Campaign.builder()
                .marketerId(campaignRequestDto.getMarketerId())
                .purpose(campaignRequestDto.getPurpose())
                .coreBenefitText(campaignRequestDto.getCoreBenefitText())
                .sourceUrl(campaignRequestDto.getSourceUrl())
                .customColumns(campaignRequestDto.getCustomColumns())
                .status("PROCESSING")
                .requestDate(LocalDateTime.now())
                .isSuccessCase(false)
                .build();
        Campaign savedCampaign = campaignRepository.save(campaign);

        webClient.post()
                .uri("/api/generate")
                .body(Mono.just(campaignRequestDto), CampaignRequestDto.class)
                .retrieve()
                .bodyToMono(AiResponseDto.class)
                .doOnError(error -> updateCampaignStatus(savedCampaign.getCampaignId(), "FAILED"))
                .subscribe(aiResponse -> {
                    saveAiResponse(savedCampaign, aiResponse);
                    updateCampaignStatus(savedCampaign.getCampaignId(), "COMPLETED");
                });

        return savedCampaign;
    }

    @Transactional
    public void saveAiResponse(Campaign campaign, AiResponseDto aiResponse) {
        List<MessageResult> messageResults = aiResponse.getTarget_groups().stream()
                .flatMap(targetGroupDto -> targetGroupDto.getMessage_drafts().stream()
                        .map(messageDraftDto -> MessageResult.builder()
                                .campaign(campaign)
                                .targetGroupIndex(targetGroupDto.getTarget_group_index())
                                .targetName(targetGroupDto.getTarget_name())
                                .targetFeatures(targetGroupDto.getTarget_features())
                                .messageDraftIndex(messageDraftDto.getMessage_draft_index())
                                .messageText(messageDraftDto.getMessage_text())
                                .validatorReport(messageDraftDto.getValidator_report())
                                .isSelected(false)
                                .build()))
                .collect(Collectors.toList());
        messageResultRepository.saveAll(messageResults);
    }

    public Page<Campaign> getAllCampaigns(LocalDate requestDate, String status, String purpose, String marketerId, Pageable pageable) {
        Specification<Campaign> spec = CampaignSpecification.withDynamicQuery(requestDate, status, purpose, marketerId);
        return campaignRepository.findAll(spec, pageable);
    }

    public Campaign getCampaignById(UUID campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("ID " + campaignId + "에 해당하는 캠페인을 찾을 수 없습니다."));
    }

    @Transactional
    public void selectMessage(UUID campaignId, UUID resultId) {
        MessageResult result = messageResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("ID " + resultId + "에 해당하는 메시지를 찾을 수 없습니다."));

        // Ensure the message belongs to the campaign
        if (!result.getCampaign().getCampaignId().equals(campaignId)) {
            throw new IllegalArgumentException("메시지가 현재 캠페인에 속해있지 않습니다.");
        }

        result.setSelected(true);
        messageResultRepository.save(result);
        updateCampaignStatus(campaignId, "MESSAGE_SELECTED");
    }

    @Transactional
    public void refineMessage(UUID campaignId, String feedback) {
        Campaign campaign = getCampaignById(campaignId); // Re-use getCampaignById for existence check
        updateCampaignStatus(campaignId, "REFINING");

        webClient.post()
                .uri("/api/refine")
                .body(Mono.just(new RefineRequestDto(feedback)), RefineRequestDto.class)
                .retrieve()
                .bodyToMono(AiResponseDto.class)
                .doOnError(error -> updateCampaignStatus(campaign.getCampaignId(), "FAILED"))
                .subscribe(aiResponse -> {
                    List<MessageResult> oldResults = messageResultRepository.findByCampaign_CampaignId(campaignId);
                    messageResultRepository.deleteAll(oldResults);
                    saveAiResponse(campaign, aiResponse);
                    updateCampaignStatus(campaign.getCampaignId(), "COMPLETED");
                });
    }

    @Transactional
    public void deleteCampaign(UUID campaignId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new ResourceNotFoundException("ID " + campaignId + "에 해당하는 캠페인을 찾을 수 없습니다.");
        }
        campaignRepository.deleteById(campaignId);
    }

    @Transactional
    public void updatePerformance(UUID campaignId, CampaignPerformanceUpdateDto performanceDto) {
        Campaign campaign = getCampaignById(campaignId); // Re-use getCampaignById for existence check
        campaign.setActualCtr(performanceDto.getActualCtr());
        campaign.setConversionRate(performanceDto.getConversionRate());
        campaign.setSuccessCase(performanceDto.getIsSuccessCase());
        campaignRepository.save(campaign);
    }

    @Transactional
    public void triggerRagRegistration(UUID campaignId) {
        Campaign campaign = getCampaignById(campaignId); // Re-use getCampaignById for existence check
        if (campaign.isSuccessCase()) {
            MessageResult selectedMessage = messageResultRepository.findByCampaign_CampaignIdAndIsSelected(campaign.getCampaignId(), true)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("성공 사례로 등록할 최종 선택된 메시지가 없습니다."));

            String title = "성공사례: " + campaign.getPurpose();
            String content = String.format(
                    "캠페인 목적: %s\n핵심 혜택: %s\n성공 메시지: %s",
                    campaign.getPurpose(),
                    campaign.getCoreBenefitText(),
                    selectedMessage.getMessageText()
            );
            SuccessCaseDto successCaseDto = new SuccessCaseDto(
                    title,
                    content,
                    "성공_사례",
                    campaign.getCampaignId().toString()
            );

            webClient.post()
                    .uri("/api/knowledge")
                    .body(Mono.just(successCaseDto), SuccessCaseDto.class)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
        } else {
            throw new IllegalStateException("성공 사례로 지정되지 않은 캠페인은 RAG DB에 등록할 수 없습니다.");
        }
    }

    @Transactional
    public void updateCampaignStatus(UUID campaignId, String newStatus) {
        Campaign campaign = getCampaignById(campaignId);
        campaign.setStatus(newStatus);
        campaignRepository.save(campaign);
    }
}
