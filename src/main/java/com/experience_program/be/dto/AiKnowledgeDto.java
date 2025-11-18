package com.experience_program.be.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class AiKnowledgeDto {
    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("campaign_summary")
    private String campaignSummary;

    @JsonProperty("campaign_details")
    private Map<String, Object> campaignDetails;

    @JsonProperty("registration_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate;
}
