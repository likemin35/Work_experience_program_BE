package com.experience_program.be.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerSegmentResponseDto {

    private String customerId;
    private String customerName;
    private String phoneNumber;
    private String targetSegment;
    private String segmentReason;
    private String customerFeatures;
}
