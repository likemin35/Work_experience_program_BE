package com.experience_program.be.dto;

import com.experience_program.be.entity.MessageResult;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MessageResultResponseDto {

    private UUID resultId;
    private int targetGroupIndex;
    private String targetName;
    private String targetFeatures;
    private String messageText;

    public static MessageResultResponseDto from(MessageResult entity) {
        return MessageResultResponseDto.builder()
                .resultId(entity.getResultId())
                .targetGroupIndex(entity.getTargetGroupIndex())
                .targetName(entity.getTargetName())
                .targetFeatures(entity.getTargetFeatures())
                .messageText(entity.getMessageText())
                .build();
    }
}
