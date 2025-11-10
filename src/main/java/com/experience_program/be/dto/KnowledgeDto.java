package com.experience_program.be.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KnowledgeDto {
    private String title;
    private String content_text;
    private String source_type;
    private Boolean is_active;
}
