package com.experience_program.be.repository;

import com.experience_program.be.entity.MessageResult;
import com.experience_program.be.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageResultRepository extends JpaRepository<MessageResult, UUID> {

    List<MessageResult> findByCampaign(Campaign campaign);

    void deleteByCampaign(Campaign campaign);
}
