package com.experience_program.be.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ClusterResultDto {

    private int clusterIndex;
    private String clusterName;
    private String clusterDescription;
    private List<String> customerIds;
}
