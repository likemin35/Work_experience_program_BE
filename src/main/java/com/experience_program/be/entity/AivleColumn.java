package com.experience_program.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "aivle_columns")
public class AivleColumn {

    @Id
    @Column(name = "column_name")
    private String columnName;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_value")
    private String defaultValue;
}
