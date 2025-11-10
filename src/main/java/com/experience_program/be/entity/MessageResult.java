package com.experience_program.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_results")
public class MessageResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "result_id", updatable = false, nullable = false)
    private UUID resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "target_group_index")
    private int targetGroupIndex;

    @Column(name = "target_name")
    private String targetName;

    @Lob
    @Column(name = "target_features", columnDefinition = "TEXT")
    private String targetFeatures;

    @Column(name = "message_draft_index")
    private int messageDraftIndex;

    @Lob
    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Lob
    @Column(name = "validator_report", columnDefinition = "TEXT")
    private String validatorReport;

    @Column(name = "is_selected")
    private boolean isSelected;
}
