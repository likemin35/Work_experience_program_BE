package com.experience_program.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

import com.experience_program.be.customer.domain.CustomerRow;
import com.experience_program.be.service.customer.dto.SegmentMatchResult;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "customer_segment",
    indexes = {
        @Index(name = "idx_campaign_id", columnList = "campaign_id"),
        @Index(name = "idx_target_segment", columnList = "target_segment")
    }
)
public class CustomerSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "segment_id", nullable = false, updatable = false)
    private UUID segmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "target_segment", nullable = false)
    private String targetSegment;

    @Lob
    @Column(name = "segment_reason")
    private String segmentReason;

    @Lob
    @Column(name = "customer_features")
    private String customerFeatures;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static CustomerSegment from(
            CustomerRow row,
            SegmentMatchResult match,
            Campaign campaign
    ) {
        return CustomerSegment.builder()
                .campaign(campaign)
                .customerId(row.getCustomerId())
                .customerName(
                    Optional.ofNullable(row.get("name"))
                        .orElse(row.get("customer_name"))
                )
                .phoneNumber(
                    Optional.ofNullable(row.get("phone"))
                        .orElse(row.get("phone_number"))
                )
                .targetSegment(match.getTargetSegment())
                .segmentReason(match.getReason())
                .customerFeatures(
                        row.getAttributes() != null
                                ? row.getAttributes().toString()
                                : null
                )
                .createdAt(LocalDateTime.now())
                .build();
    }
}
