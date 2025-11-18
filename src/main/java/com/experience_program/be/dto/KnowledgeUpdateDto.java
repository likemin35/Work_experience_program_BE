package com.experience_program.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class KnowledgeUpdateDto {
    @JsonProperty("campaign_summary")
    private String campaignSummary;

    @JsonProperty("campaign_details")
    private Map<String, Object> campaignDetails;
}
