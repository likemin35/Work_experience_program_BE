package com.experience_program.be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSummaryDto {
    private long ongoingCampaigns;
    private long successCases;
    private long totalKnowledge;
}
