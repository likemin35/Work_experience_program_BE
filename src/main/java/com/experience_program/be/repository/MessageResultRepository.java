package com.experience_program.be.repository;

import com.experience_program.be.entity.MessageResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageResultRepository extends JpaRepository<MessageResult, UUID> {
    List<MessageResult> findByCampaign_CampaignId(UUID campaignId);
    List<MessageResult> findByCampaign_CampaignIdAndIsSelected(UUID campaignId, boolean isSelected);
}
