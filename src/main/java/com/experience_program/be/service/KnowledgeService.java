package com.experience_program.be.service;

import com.experience_program.be.dto.KnowledgeDto;
import com.experience_program.be.entity.KnowledgeBase;
import com.experience_program.be.repository.KnowledgeBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class KnowledgeService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Autowired
    public KnowledgeService(KnowledgeBaseRepository knowledgeBaseRepository) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
    }

    @Transactional
    public KnowledgeBase createKnowledge(KnowledgeDto knowledgeDto) {
        KnowledgeBase knowledgeBase = KnowledgeBase.builder()
                .title(knowledgeDto.getTitle())
                .contentText(knowledgeDto.getContent_text())
                .sourceType(knowledgeDto.getSource_type())
                .uploadDate(LocalDateTime.now())
                .isActive(knowledgeDto.getIs_active() != null ? knowledgeDto.getIs_active() : true)
                .build();
        // In a real scenario, this would also trigger vectorization and storage in a vector DB.
        return knowledgeBaseRepository.save(knowledgeBase);
    }

    public List<KnowledgeBase> findAllKnowledge() {
        return knowledgeBaseRepository.findAll();
    }

    public Optional<KnowledgeBase> findKnowledgeById(UUID knowledgeId) {
        return knowledgeBaseRepository.findById(knowledgeId);
    }

    @Transactional
    public Optional<KnowledgeBase> updateKnowledge(UUID knowledgeId, KnowledgeDto knowledgeDto) {
        return knowledgeBaseRepository.findById(knowledgeId)
                .map(existingKnowledge -> {
                    existingKnowledge.setTitle(knowledgeDto.getTitle());
                    existingKnowledge.setContentText(knowledgeDto.getContent_text());
                    existingKnowledge.setSourceType(knowledgeDto.getSource_type());
                    if (knowledgeDto.getIs_active() != null) {
                        existingKnowledge.setActive(knowledgeDto.getIs_active());
                    }
                    // Re-vectorization might be needed here.
                    return knowledgeBaseRepository.save(existingKnowledge);
                });
    }

    @Transactional
    public boolean deleteKnowledge(UUID knowledgeId) {
        if (knowledgeBaseRepository.existsById(knowledgeId)) {
            knowledgeBaseRepository.deleteById(knowledgeId);
            return true;
        }
        return false;
    }
}
