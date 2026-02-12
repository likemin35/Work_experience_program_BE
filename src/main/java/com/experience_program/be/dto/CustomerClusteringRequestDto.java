package com.experience_program.be.dto;

import com.experience_program.be.entity.Campaign;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerClusteringRequestDto {

    private CampaignDto campaign;
    private List<CustomerDto> customers;

    @Getter
    @Builder
    public static class CampaignDto {
        private String title;
        private String coreBenefitText;
    }

    @Getter
    @Builder
    public static class CustomerDto {
        private String customerId;
        private String description;
    }

    public static CustomerClusteringRequestDto from(
            Campaign campaign,
            List<CustomerDto> customers
    ) {
        return CustomerClusteringRequestDto.builder()
                .campaign(
                        CampaignDto.builder()
                                .title(campaign.getTitle())
                                .coreBenefitText(campaign.getCoreBenefitText())
                                .build()
                )
                .customers(customers)
                .build();
    }
}
