package com.experience_program.be.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonRawValue;
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
    @JsonBackReference
    private Campaign campaign;

    @Column(name = "target_group_index")
    private int targetGroupIndex;

    @Column(name = "target_name")
    private String targetName;

    @Lob
    @Column(name = "target_features", columnDefinition = "TEXT")
    private String targetFeatures;

    @Lob
    @Column(name = "classification_reason", columnDefinition = "TEXT")
    private String classificationReason;

    @Lob
    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Lob
    @Column(name = "validator_report", columnDefinition = "TEXT")
    @JsonRawValue
    private String validatorReport;
}
