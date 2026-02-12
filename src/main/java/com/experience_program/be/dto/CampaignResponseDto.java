package com.experience_program.be.dto;

import com.experience_program.be.entity.Campaign;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CampaignResponseDto {

    private String campaignId;
    private String title;
    private String status;

    public static CampaignResponseDto from(Campaign campaign) {
        return new CampaignResponseDto(
                campaign.getCampaignId(),
                campaign.getTitle(),
                campaign.getStatus()
        );
    }
}
