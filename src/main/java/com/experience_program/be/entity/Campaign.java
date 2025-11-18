package com.experience_program.be.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "campaign_id", updatable = false, nullable = false)
    private UUID campaignId;

    @CreationTimestamp
    @Column(name = "request_date", nullable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column(name = "marketer_id", length = 255)
    private String marketerId;

    @Column(name = "purpose", length = 255)
    private String purpose;

    @Lob
    @Column(name = "core_benefit_text", columnDefinition = "TEXT")
    private String coreBenefitText;

    @Column(name = "source_url", length = 2083)
    private String sourceUrl;

    @Lob
    @Column(name = "custom_columns", columnDefinition = "TEXT")
    private String customColumns;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "actual_ctr", precision = 5, scale = 2)
    private BigDecimal actualCtr;

    @Column(name = "conversion_rate", precision = 5, scale = 2)
    private BigDecimal conversionRate;

    @Column(name = "is_success_case")
    private boolean isSuccessCase;

    @Column(name = "is_performance_registered", nullable = false, columnDefinition = "boolean default false")
    private boolean isPerformanceRegistered = false;

    @Column(name = "is_rag_registered", nullable = false, columnDefinition = "boolean default false")
    private boolean isRagRegistered = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<MessageResult> messageResults = new ArrayList<>();
}
