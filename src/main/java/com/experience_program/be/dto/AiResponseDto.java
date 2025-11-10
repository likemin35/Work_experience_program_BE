package com.experience_program.be.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiResponseDto {
    private List<TargetGroupDto> target_groups;
}
