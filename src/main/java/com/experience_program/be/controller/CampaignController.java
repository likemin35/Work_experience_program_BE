package com.experience_program.be.controller;

import com.experience_program.be.dto.CampaignRequestDto;
import com.experience_program.be.dto.CampaignResponseDto;
import com.experience_program.be.dto.MessageResultResponseDto;
import com.experience_program.be.dto.MessageDraftDto;
import com.experience_program.be.dto.CustomerSegmentResponseDto;
import com.experience_program.be.dto.CustomerSegmentMessageDto;
import com.experience_program.be.service.CampaignService;
import com.experience_program.be.client.AiCampaignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final AiCampaignClient aiCampaignClient;

        public CampaignController(
                CampaignService campaignService,
                AiCampaignClient aiCampaignClient
        ) {
                this.campaignService = campaignService;
                this.aiCampaignClient = aiCampaignClient;
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<CampaignResponseDto> createCampaign(
                @RequestPart("file") MultipartFile pdfFile
        ) {
        CampaignRequestDto dto =
                aiCampaignClient.extractCampaignFromPdf(pdfFile);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        CampaignResponseDto.from(
                        campaignService.createCampaign(dto)
                        )
                );
        }

    // CSV 업로드 + 고객 세그먼트 생성
    @PostMapping("/{campaign_id}/segments")
    public ResponseEntity<Void> uploadAndSegment(
            @PathVariable("campaign_id") String campaignId,
            @RequestParam("file") MultipartFile file
    ) {
        campaignService.uploadCsvAndSegment(campaignId, file);
        return ResponseEntity.ok().build();
    }

    // 세그먼트별 메시지 생성 트리거 (실제 생성 로직은 서비스/AI 쪽)
    @PostMapping("/{campaign_id}/messages")
    public ResponseEntity<Void> generateMessages(
            @PathVariable("campaign_id") String campaignId
    ) {
        campaignService.generateMessagesBySegment(campaignId);
        return ResponseEntity.accepted().build();
    }

    // 세그먼트 결과 조회
    @GetMapping("/{campaign_id}/segments")
    public ResponseEntity<List<CustomerSegmentResponseDto>> getCustomerSegments(
            @PathVariable("campaign_id") String campaignId
    ) {
        return ResponseEntity.ok(
                campaignService.getCustomerSegments(campaignId)
        );
    }

    // 세그먼트 결과 CSV 다운로드
    @GetMapping("/{campaign_id}/segments/csv")
    public ResponseEntity<byte[]> downloadCustomerSegmentsCsv(
            @PathVariable("campaign_id") String campaignId
    ) {
        byte[] csv = campaignService.downloadCustomerSegmentsCsv(campaignId);

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"customer_segments_" + campaignId + ".csv\""
                )
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csv);
    }
    
    @GetMapping("/{campaign_id}/messages")
    public ResponseEntity<List<MessageResultResponseDto>> getMessages(
            @PathVariable("campaign_id") String campaignId
    ) {
        return ResponseEntity.ok(
                campaignService.getMessagesByCampaign(campaignId)
        );
    }


    // 프로모션 목록 조회
    @GetMapping
    public ResponseEntity<List<CampaignResponseDto>> getCampaigns() {
        return ResponseEntity.ok(
                campaignService.getAllCampaigns()
        );
    }

    // 메시지 매핑 결과 조회
    @GetMapping("/{campaignId}/messages/map")
    public ResponseEntity<List<CustomerSegmentMessageDto>> mapMessages(
            @PathVariable UUID campaignId
    ) {
        return ResponseEntity.ok(
                campaignService.mapCustomerMessagesFromDb(
                        campaignId.toString()
                )
        );
    }
    // 메시지 매핑 결과 csv 다운로드
    @GetMapping("/{campaignId}/messages/csv")
    public ResponseEntity<byte[]> downloadMessageCsv(
            @PathVariable UUID campaignId
    ) {
        byte[] csvBytes =
                campaignService.downloadCustomerMessageCsvFromDb(
                        campaignId.toString()
                );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"customer_messages.csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csvBytes);
    }

    @PatchMapping("/messages/{result_id}")
    public ResponseEntity<Void> updateMessageDirect(
            @PathVariable("result_id") UUID resultId,
            @RequestBody MessageDraftDto dto
    ) {
        campaignService.updateMessageDraft(resultId, dto);
        return ResponseEntity.ok().build();
    }
}
