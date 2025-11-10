package com.experience_program.be.controller;

import com.experience_program.be.entity.Campaign;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CampaignSpecification {

    public static Specification<Campaign> withDynamicQuery(LocalDate requestDate, String status, String purpose, String marketerId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (requestDate != null) {
                predicates.add(criteriaBuilder.equal(root.get("requestDate").as(LocalDate.class), requestDate));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(purpose)) {
                predicates.add(criteriaBuilder.like(root.get("purpose"), "%" + purpose + "%"));
            }

            if (StringUtils.hasText(marketerId)) {
                predicates.add(criteriaBuilder.equal(root.get("marketerId"), marketerId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}