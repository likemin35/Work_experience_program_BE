package com.experience_program.be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SuccessCaseDto {
    private String title;
    private String content_text;
    private String source_type;
    private String related_campaign_id;
}
