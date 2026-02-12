package com.experience_program.be.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CampaignRequestDto {

    @NotBlank
    private String marketerId;

    @NotBlank
    private String title;

    @NotBlank
    private String coreBenefitText;

    private List<String> sourceUrl;

    private Map<String, Object> customColumns;
}
