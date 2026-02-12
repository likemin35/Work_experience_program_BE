// SegmentMatchResult.java
package com.experience_program.be.service.customer.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SegmentMatchResult {

    private String targetSegment;
    private String reason;
    private double similarityScore;
}
