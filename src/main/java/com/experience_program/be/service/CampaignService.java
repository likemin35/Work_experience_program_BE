package com.experience_program.be.service;

import com.experience_program.be.dto.*;
import com.experience_program.be.entity.Campaign;
import com.experience_program.be.entity.CustomerSegment;
import com.experience_program.be.entity.MessageResult;
import com.experience_program.be.repository.MessageResultRepository;
import com.experience_program.be.repository.CampaignRepository;
import com.experience_program.be.repository.CustomerSegmentRepository;
import com.experience_program.be.customer.domain.CustomerRow;
import com.experience_program.be.service.customer.CustomerCsvService;
import com.experience_program.be.service.customer.CustomerDescriptionBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Sort;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CustomerSegmentRepository customerSegmentRepository;
    private final MessageResultRepository messageResultRepository;

    private final CustomerCsvService customerCsvService;
    private final CustomerDescriptionBuilder customerDescriptionBuilder;

    private final RestClient aiRestClient;
    private final ObjectMapper objectMapper;

    /* =========================
       캠페인 생성
       ========================= */
    @Transactional
    public Campaign createCampaign(CampaignRequestDto dto) {

        Campaign campaign = Campaign.builder()
                .marketerId(dto.getMarketerId())
                .title(dto.getTitle())
                .coreBenefitText(dto.getCoreBenefitText())
                .sourceUrl(convertObjectToJson(dto.getSourceUrl()))
                .customColumns(convertObjectToJson(dto.getCustomColumns()))
                .status("CREATED")
                .requestDate(LocalDateTime.now())
                .build();

        return campaignRepository.save(campaign);
    }


    /* =========================
       CSV 업로드 + 세그먼트 생성
       ========================= */
    @Transactional
    public void uploadCsvAndSegment(String campaignId, MultipartFile file) {

        log.info("uploadCsvAndSegment campaignId={}", campaignId);

        Campaign campaign = getCampaignById(campaignId);

        List<CustomerRow> rows = customerCsvService.parse(file);

        Map<String, CustomerRow> rowMap = rows.stream()
                .collect(Collectors.toMap(
                        CustomerRow::getCustomerId,
                        r -> r,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<CustomerClusteringRequestDto.CustomerDto> customerDtos =
                rows.stream()
                        .map(r -> CustomerClusteringRequestDto.CustomerDto.builder()
                                .customerId(r.getCustomerId())
                                .description(customerDescriptionBuilder.build(r))
                                .build())
                        .toList();

        CustomerClusteringRequestDto requestDto =
                CustomerClusteringRequestDto.from(campaign, customerDtos);

        CustomerClusteringResponseDto response =
                aiRestClient.post()
                        .uri("/cluster-customers")
                        .body(requestDto)
                        .retrieve()
                        .body(CustomerClusteringResponseDto.class);

        log.info("AI clustering response={}", response);

        if (response == null || response.getClusters() == null) {
            throw new IllegalStateException("세그먼트 생성 실패");
        }

        customerSegmentRepository.deleteByCampaign(campaign);

        Set<String> processedCustomerIds = new HashSet<>();

        for (ClusterResultDto cluster : response.getClusters()) {
            for (String cid : cluster.getCustomerIds()) {

                if (!processedCustomerIds.add(cid)) continue;

                CustomerRow row = rowMap.get(cid);

                customerSegmentRepository.save(
                        CustomerSegment.builder()
                                .campaign(campaign)
                                .customerId(cid)
                                .customerName(row != null ? row.get("name") : "")
                                .phoneNumber(
                                row != null
                                        ? Optional.ofNullable(row.get("phone_number"))
                                        .orElse(row.get("phoneNumber"))
                                        : ""
                                )
                                .targetSegment(cluster.getClusterName())
                                .segmentReason(cluster.getClusterDescription())
                                .customerFeatures(
                                        row != null ? customerDescriptionBuilder.build(row) : ""
                                )
                                .createdAt(LocalDateTime.now())
                                .build()
                );
            }
        }

        campaign.setStatus("SEGMENTED");
        campaignRepository.save(campaign);
    }

    /* =========================
       세그먼트 조회
       ========================= */
    @Transactional(readOnly = true)
    public List<CustomerSegmentResponseDto> getCustomerSegments(String campaignId) {

        Campaign campaign = getCampaignById(campaignId);
        log.info("campaign status = {}", campaign.getStatus());
        if (!List.of("SEGMENTED", "MESSAGE_GENERATED").contains(campaign.getStatus())) {
            throw new IllegalStateException("세그먼트가 생성되지 않은 캠페인입니다.");
        }

        return customerSegmentRepository
                .findByCampaign(campaign)
                .stream()
                .sorted(Comparator.comparingInt(
                    s -> Integer.parseInt(s.getCustomerId())
                ))
                .map(s -> CustomerSegmentResponseDto.builder()
                        .customerId(s.getCustomerId())
                        .customerName(s.getCustomerName())
                        .phoneNumber(s.getPhoneNumber())
                        .targetSegment(s.getTargetSegment())
                        .segmentReason(s.getSegmentReason())
                        .customerFeatures(s.getCustomerFeatures())
                        .build())
                .toList();
    }


    /* =========================
       CSV 다운로드
       ========================= */
    @Transactional(readOnly = true)
    public byte[] downloadCustomerSegmentsCsv(String campaignId) {

        Campaign campaign = getCampaignById(campaignId);

        List<CustomerSegment> segments =
                customerSegmentRepository.findByCampaign(campaign);
        
        if (segments.isEmpty()) {
            throw new IllegalStateException("생성된 세그먼트가 없습니다.");
        }

        segments.sort(Comparator.comparingInt(
                    s -> Integer.parseInt(s.getCustomerId())
                ));


        StringBuilder sb = new StringBuilder();
        sb.append("customer_id,customer_name,phone_number,target_segment,segment_reason,customer_features\n");

        for (CustomerSegment s : segments) {
            sb.append(escape(s.getCustomerId())).append(",")
              .append(escape(s.getCustomerName())).append(",")
              .append(escape(s.getPhoneNumber())).append(",")
              .append(escape(s.getTargetSegment())).append(",")
              .append(escape(s.getSegmentReason())).append(",")
              .append(escape(s.getCustomerFeatures())).append("\n");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }

    @Transactional
    public void generateMessagesBySegment(String campaignId) {

        Campaign campaign = getCampaignById(campaignId);

        if ("CREATED".equals(campaign.getStatus())) {
            throw new IllegalStateException("세그먼트 생성 후에만 메시지를 생성할 수 있습니다.");
        }

        // 1. 고객 세그먼트 조회
        List<CustomerSegment> segments =
                customerSegmentRepository.findByCampaign(campaign);

        if (segments.isEmpty()) {
            throw new IllegalStateException("생성된 세그먼트가 없습니다.");
        }

        // 2. 세그먼트명 기준으로 그룹핑
        Map<String, List<CustomerSegment>> grouped =
                segments.stream()
                        .collect(Collectors.groupingBy(CustomerSegment::getTargetSegment));

        // 3. AI에 넘길 세그먼트 단위 입력 생성
        List<Map<String, String>> aiSegments =
                grouped.entrySet().stream()
                        .map(entry -> {
                            String targetSegment = entry.getKey();
                            List<CustomerSegment> groupSegments = entry.getValue();

                            String mergedFeatures =
                                    groupSegments.stream()
                                            .map(CustomerSegment::getCustomerFeatures)
                                            .filter(Objects::nonNull)
                                            .flatMap(f -> Arrays.stream(f.split("\n")))
                                            .map(String::trim)
                                            .filter(s -> !s.isEmpty())
                                            .collect(Collectors.groupingBy(
                                                    s -> s,
                                                    Collectors.counting()
                                            ))
                                            .entrySet().stream()
                                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                            .limit(10)
                                            .map(Map.Entry::getKey)
                                            .collect(Collectors.joining("\n"));

                            return Map.of(
                                    "target_segment", targetSegment,
                                    "segment_features", mergedFeatures
                            );
                        })
                        .toList();

        Map<String, Object> aiRequest = Map.of(
                "title", campaign.getTitle(),
                "sourceUrl", campaign.getSourceUrl(),
                "coreBenefitText", campaign.getCoreBenefitText(),
                "segments", aiSegments
        );

        Map<String, Object> aiResponse =
                aiRestClient.post()
                        .uri("/generate-messages")
                        .body(aiRequest)
                        .retrieve()
                        .body(Map.class);

        if (aiResponse == null || !aiResponse.containsKey("messages")) {
            throw new IllegalStateException("AI 메시지 생성 실패");
        }

        messageResultRepository.deleteByCampaign(campaign);

        List<Map<String, Object>> messages =
                (List<Map<String, Object>>) aiResponse.get("messages");

        for (Map<String, Object> msgGroup : messages) {

            int targetGroupIndex =
                    ((Number) msgGroup.get("target_group_index")).intValue();
            String targetName = (String) msgGroup.get("target_name");

            List<Map<String, Object>> drafts =
                    (List<Map<String, Object>>) msgGroup.get("message_drafts");

            for (Map<String, Object> draft : drafts) {

                MessageResult result = MessageResult.builder()
                        .campaign(campaign)
                        .targetGroupIndex(targetGroupIndex)
                        .targetName(targetName)
                        .messageText((String) draft.get("message_text"))
                        .build();

                messageResultRepository.save(result);
            }
        }

        campaign.setStatus("MESSAGE_GENERATED");
        campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public List<CustomerSegmentMessageDto> mapCustomerMessages(
            String campaignId,
            Map<String, String> segmentMessageMap
    ) {
        Campaign campaign = getCampaignById(campaignId);

        List<CustomerSegment> segments =
                customerSegmentRepository.findByCampaign(campaign);

        if (segments.isEmpty()) {
            throw new IllegalStateException("생성된 세그먼트가 없습니다.");
        }

        segments.sort(Comparator.comparingInt(
                    s -> Integer.parseInt(s.getCustomerId())
                ));

        return segments.stream()
                .map(s -> {
                    String template = segmentMessageMap.get(s.getTargetSegment());
                    String message = applyTemplate(template, s.getCustomerName());

                    return new CustomerSegmentMessageDto(
                            s.getCustomerId(),
                            s.getCustomerName(),
                            s.getPhoneNumber(),
                            s.getTargetSegment(),
                            s.getSegmentReason(),
                            s.getCustomerFeatures(),
                            message
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] downloadCustomerMessageCsv(
            String campaignId,
            Map<String, String> segmentMessageMap
    ) {
        Campaign campaign = getCampaignById(campaignId);

        List<CustomerSegment> segments =
                customerSegmentRepository.findByCampaign(campaign);

        if (segments.isEmpty()) {
            throw new IllegalStateException("생성된 세그먼트가 없습니다.");
        }

        segments.sort(Comparator.comparingInt( 
                    s -> Integer.parseInt(s.getCustomerId()) 
                ));

        StringBuilder sb = new StringBuilder();
        sb.append("customer_id,customer_name,phone_number,target_segment,segment_reason,customer_features,message\n");

        for (CustomerSegment s : segments) {
            String template = segmentMessageMap.get(s.getTargetSegment());
            String message = applyTemplate(template, s.getCustomerName());

            sb.append(escape(s.getCustomerId())).append(",")
            .append(escape(s.getCustomerName())).append(",")
            .append(escape(s.getPhoneNumber())).append(",")
            .append(escape(s.getTargetSegment())).append(",")
            .append(escape(s.getSegmentReason())).append(",")
            .append(escape(s.getCustomerFeatures())).append(",")
            .append(escape(message)).append("\n");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }



    @Transactional
    public void updateMessageDraft(UUID resultId, MessageDraftDto dto) {

        MessageResult result = messageResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        result.setMessageText(dto.getMessageText());

        messageResultRepository.save(result);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponseDto> getAllCampaigns() {
        return campaignRepository
                .findAll(Sort.by(Sort.Direction.DESC, "requestDate"))
                .stream()
                .map(CampaignResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerSegmentMessageDto> mapCustomerMessagesFromDb(
            String campaignId
    ) {
        Campaign campaign = getCampaignById(campaignId);

        List<CustomerSegment> segments =
                customerSegmentRepository.findByCampaign(campaign);

        List<MessageResult> messages =
                messageResultRepository.findByCampaign(campaign);

        Map<String, String> segmentMessageMap =
                messages.stream()
                        .collect(Collectors.toMap(
                                MessageResult::getTargetName,
                                MessageResult::getMessageText,
                                (a, b) -> a
                        ));

        return segments.stream()
                .map(s -> {
                    String template =
                            segmentMessageMap.get(s.getTargetSegment());
                    String message =
                            applyTemplate(template, s.getCustomerName());

                    return new CustomerSegmentMessageDto(
                            s.getCustomerId(),
                            s.getCustomerName(),
                            s.getPhoneNumber(),
                            s.getTargetSegment(),
                            s.getSegmentReason(),
                            s.getCustomerFeatures(),
                            message
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] downloadCustomerMessageCsvFromDb(String campaignId) {

        Campaign campaign = getCampaignById(campaignId);

        List<CustomerSegment> segments =
                customerSegmentRepository.findByCampaign(campaign);

        if (segments.isEmpty()) {
            throw new IllegalStateException("고객 세그먼트가 없습니다.");
        }

        List<MessageResult> messages =
                messageResultRepository.findByCampaign(campaign);

        Map<String, String> segmentMessageMap =
                messages.stream()
                        .collect(Collectors.toMap(
                                MessageResult::getTargetName,
                                MessageResult::getMessageText,
                                (a, b) -> a
                        ));

        segments.sort(Comparator.comparingInt( 
                    s -> Integer.parseInt(s.getCustomerId()) 
                ));

        StringBuilder sb = new StringBuilder();
        sb.append(
            "customer_id,customer_name,phone_number,target_segment,segment_reason,customer_features,message\n"
        );

        for (CustomerSegment s : segments) {

            String template = segmentMessageMap.get(s.getTargetSegment());
            String message =
                    template != null
                            ? template.replace("{name}", s.getCustomerName())
                            : "";

            sb.append(escape(s.getCustomerId())).append(",")
            .append(escape(s.getCustomerName())).append(",")
            .append(escape(s.getPhoneNumber())).append(",")
            .append(escape(s.getTargetSegment())).append(",")
            .append(escape(s.getSegmentReason())).append(",")
            .append(escape(s.getCustomerFeatures())).append(",")
            .append(escape(message)).append("\n");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }


    /* =========================
       내부 유틸
       ========================= */
    public Campaign getCampaignById(String campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("캠페인을 찾을 수 없습니다."));
    }

    private String convertObjectToJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 변환 실패", e);
        }
    }

    private String applyTemplate(String template, String customerName) {
        if (template == null) return "";
        if (customerName == null) customerName = "";
        return template.replace("{name}", customerName);
    }

    @Transactional(readOnly = true)
    public List<MessageResultResponseDto> getMessagesByCampaign(String campaignId) {

        Campaign campaign = getCampaignById(campaignId);

        return messageResultRepository
                .findByCampaign(campaign)
                .stream()
                .map(MessageResultResponseDto::from)
                .toList();
    }

    private String escape(String v) {
        if (v == null) return "";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
    private String buildRepresentativeFeatures(
            List<CustomerSegment> segments,
            int limit
    ) {
        return segments.stream()
                .map(CustomerSegment::getCustomerFeatures)
                .filter(Objects::nonNull)
                .flatMap(f -> Arrays.stream(f.split("\n")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(
                        s -> s,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("\n"));
    }
}
