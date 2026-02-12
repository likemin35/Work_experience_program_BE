package com.experience_program.be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CustomerSegmentMessageDto {

    private String customerId;
    private String customerName;
    private String phoneNumber;
    private String targetSegment;
    private String segmentReason;
    private String customerFeatures;
    private String messageText;
}
