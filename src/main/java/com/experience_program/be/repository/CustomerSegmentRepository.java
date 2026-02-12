package com.experience_program.be.repository;

import com.experience_program.be.entity.Campaign;
import com.experience_program.be.entity.CustomerSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerSegmentRepository extends JpaRepository<CustomerSegment, UUID> {

    List<CustomerSegment> findByCampaign(Campaign campaign);

    boolean existsByCampaign(Campaign campaign);

    void deleteByCampaign(Campaign campaign);
}
