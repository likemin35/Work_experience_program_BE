package com.experience_program.be.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CampaignPerformanceUpdateDto {
    private BigDecimal actualCtr;
    private BigDecimal conversionRate;
    private Boolean isSuccessCase;
}
