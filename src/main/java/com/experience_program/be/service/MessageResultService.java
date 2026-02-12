package com.experience_program.be.service;

import com.experience_program.be.entity.Campaign;
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
    private final CampaignService campaignService;

    @Autowired
    public MessageResultService(
            MessageResultRepository messageResultRepository,
            CampaignService campaignService
    ) {
        this.messageResultRepository = messageResultRepository;
        this.campaignService = campaignService;
    }

    public MessageResult saveMessageResult(MessageResult messageResult) {
        return messageResultRepository.save(messageResult);
    }

    public Optional<MessageResult> getMessageResultById(UUID resultId) {
        return messageResultRepository.findById(resultId);
    }

    public List<MessageResult> getMessageResultsByCampaignId(String campaignId) {
        Campaign campaign = campaignService.getCampaignById(campaignId);
        return messageResultRepository.findByCampaign(campaign);
    }

    public boolean selectMessage(UUID resultId) {
        Optional<MessageResult> optionalMessageResult = messageResultRepository.findById(resultId);
        if (optionalMessageResult.isPresent()) {
            MessageResult messageResult = optionalMessageResult.get();
            messageResultRepository.save(messageResult);
            return true;
        }
        return false;
    }

    public boolean deselectMessage(UUID resultId) {
        Optional<MessageResult> optionalMessageResult = messageResultRepository.findById(resultId);
        if (optionalMessageResult.isPresent()) {
            MessageResult messageResult = optionalMessageResult.get();
            messageResultRepository.save(messageResult);
            return true;
        }
        return false;
    }
}
