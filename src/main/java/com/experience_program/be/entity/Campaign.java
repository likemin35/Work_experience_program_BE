package com.experience_program.be.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @Column(name = "campaign_id", length = 36, nullable = false, updatable = false)
    private String campaignId;

    @PrePersist
    public void prePersist() {
        if (this.campaignId == null) {
            this.campaignId = UUID.randomUUID().toString();
        }
    }

    @CreationTimestamp
    @Column(name = "request_date", nullable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column(name = "marketer_id", length = 255)
    private String marketerId;

    @Column(name = "title", length = 255)
    private String title;

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

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "campaign",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonManagedReference
    @Builder.Default
    private List<MessageResult> messageResults = new ArrayList<>();
}
