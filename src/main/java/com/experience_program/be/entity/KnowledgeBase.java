package com.experience_program.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "knowledge_base")
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "knowledge_id", updatable = false, nullable = false)
    private UUID knowledgeId;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @CreationTimestamp
    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "related_campaign_id")
    private UUID relatedCampaignId;
}
