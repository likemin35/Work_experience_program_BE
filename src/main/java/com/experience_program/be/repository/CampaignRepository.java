package com.experience_program.be.repository;

import com.experience_program.be.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
}
