package com.experience_program.be.client;

import com.experience_program.be.dto.CampaignRequestDto;
import com.experience_program.be.dto.AiCampaignExtractDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class AiCampaignClient {

    private final RestClient restClient;

    public AiCampaignClient(
            RestClient.Builder restClientBuilder,
            @Value("${ai.server.url}") String aiServerUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(aiServerUrl)
                .build();
    }

    public CampaignRequestDto extractCampaignFromPdf(MultipartFile file) {
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", resource)
                    .contentType(MediaType.APPLICATION_PDF);

            AiCampaignExtractDto aiDto = restClient.post()
                    .uri("/ai/campaign/extract")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(bodyBuilder.build())
                    .retrieve()
                    .body(AiCampaignExtractDto.class);

            if (aiDto == null) {
                throw new RuntimeException("AI 응답이 null");
            }

            CampaignRequestDto dto = new CampaignRequestDto();
            dto.setMarketerId("tester");
            dto.setTitle(aiDto.getTitle());
            dto.setCoreBenefitText(aiDto.getCoreBenefitText());

            if (aiDto.getSourceUrl() != null && !aiDto.getSourceUrl().isBlank()) {
                dto.setSourceUrl(List.of(aiDto.getSourceUrl()));
            }

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("AI 서버 PDF 파싱 실패", e);
        }
    }
}
