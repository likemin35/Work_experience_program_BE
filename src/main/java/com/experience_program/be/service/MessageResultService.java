package com.experience_program.be.service;

import com.experience_program.be.entity.MessageResult;
import com.experience_program.be.repository.MessageResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageResultService {

    private final MessageResultRepository messageResultRepository;

    @Autowired
    public MessageResultService(MessageResultRepository messageResultRepository) {
        this.messageResultRepository = messageResultRepository;
    }

    public MessageResult saveMessageResult(MessageResult messageResult) {
        return messageResultRepository.save(messageResult);
    }

    public Optional<MessageResult> getMessageResultById(UUID resultId) {
        return messageResultRepository.findById(resultId);
    }

    public List<MessageResult> getMessageResultsByCampaignId(UUID campaignId) {
        return messageResultRepository.findByCampaign_CampaignId(campaignId);
    }

    public boolean selectMessage(UUID resultId) {
        Optional<MessageResult> optionalMessageResult = messageResultRepository.findById(resultId);
        if (optionalMessageResult.isPresent()) {
            MessageResult messageResult = optionalMessageResult.get();
            messageResult.setSelected(true);
            messageResultRepository.save(messageResult);
            return true;
        }
        return false;
    }

    public boolean deselectMessage(UUID resultId) {
        Optional<MessageResult> optionalMessageResult = messageResultRepository.findById(resultId);
        if (optionalMessageResult.isPresent()) {
            MessageResult messageResult = optionalMessageResult.get();
            messageResult.setSelected(false);
            messageResultRepository.save(messageResult);
            return true;
        }
        return false;
    }
}
