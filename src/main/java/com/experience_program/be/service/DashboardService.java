package com.experience_program.be.service;

import com.experience_program.be.entity.Campaign;
import com.experience_program.be.dto.DashboardSummaryDto;
import com.experience_program.be.repository.CampaignRepository;
import com.experience_program.be.repository.KnowledgeBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DashboardService {

    private final CampaignRepository campaignRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Autowired
    public DashboardService(CampaignRepository campaignRepository, KnowledgeBaseRepository knowledgeBaseRepository) {
        this.campaignRepository = campaignRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
    }

    public DashboardSummaryDto getDashboardSummary() {
        List<String> ongoingStatuses = Arrays.asList("CREATED", "REFINING", "MESSAGE_SELECTED");
        long ongoingCampaigns = campaignRepository.countByStatusIn(ongoingStatuses);
        long successCases = campaignRepository.countByIsSuccessCase(true);
        long totalKnowledge = knowledgeBaseRepository.count();
        return new DashboardSummaryDto(ongoingCampaigns, successCases, totalKnowledge);
    }

    public List<Campaign> getRecentActivity() {
        return campaignRepository.findTop5ByOrderByRequestDateDesc();
    }
}