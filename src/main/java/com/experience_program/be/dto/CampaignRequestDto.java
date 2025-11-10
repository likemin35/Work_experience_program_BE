package com.experience_program.be.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CampaignRequestDto {

    @NotBlank(message = "마케터 ID는 필수 입력 값입니다.")
    private String marketerId;

    @NotBlank(message = "캠페인 목적은 필수 입력 값입니다.")
    @Size(max = 255, message = "캠페인 목적은 255자를 초과할 수 없습니다.")
    private String purpose;

    @NotBlank(message = "핵심 혜택 내용은 필수 입력 값입니다.")
    private String coreBenefitText;

    private String sourceUrl;

    private String customColumns; // JSON string
}
