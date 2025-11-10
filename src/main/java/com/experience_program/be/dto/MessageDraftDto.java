package com.experience_program.be.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDraftDto {
    private int message_draft_index;
    private String message_text;
    private String validator_report;
}
