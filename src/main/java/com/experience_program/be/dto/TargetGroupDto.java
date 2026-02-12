package com.experience_program.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TargetGroupDto {
    @JsonProperty("target_group_index")
    private int target_group_index;

    @JsonProperty("target_name")
    private String target_name;

    @JsonProperty("target_features")
    private String target_features;

    @JsonProperty("classification_reason")
    private String classification_reason;

}
