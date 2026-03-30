package com.experience_program.be.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.List;

@Getter
@Setter
public class CustomerClusteringResponseDto {

    private List<ClusterResultDto> clusters;
    private Map<String, Object> strategyMeta;
}
