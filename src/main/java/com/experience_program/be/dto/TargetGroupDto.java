package com.experience_program.be.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TargetGroupDto {
    private int target_group_index;
    private String target_name;
    private String target_features;
    private List<MessageDraftDto> message_drafts;
}
