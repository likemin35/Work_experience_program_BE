package com.experience_program.be.controller;

import com.experience_program.be.dto.KnowledgeDto;
import com.experience_program.be.entity.KnowledgeBase;
import com.experience_program.be.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @Autowired
    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    // 지식 등록 (정책/팩트)
    @PostMapping
    public ResponseEntity<KnowledgeBase> createKnowledge(@RequestBody KnowledgeDto knowledgeDto) {
        KnowledgeBase createdKnowledge = knowledgeService.createKnowledge(knowledgeDto);
        return ResponseEntity.ok(createdKnowledge);
    }

    // 지식 목록 조회
    @GetMapping
    public ResponseEntity<List<KnowledgeBase>> getAllKnowledge() {
        List<KnowledgeBase> knowledgeList = knowledgeService.findAllKnowledge();
        return ResponseEntity.ok(knowledgeList);
    }

    // 지식 상세 조회
    @GetMapping("/{knowledge_id}")
    public ResponseEntity<KnowledgeBase> getKnowledgeById(@PathVariable("knowledge_id") UUID knowledgeId) {
        return knowledgeService.findKnowledgeById(knowledgeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 지식 수정
    @PutMapping("/{knowledge_id}")
    public ResponseEntity<KnowledgeBase> updateKnowledge(@PathVariable("knowledge_id") UUID knowledgeId, @RequestBody KnowledgeDto knowledgeDto) {
        return knowledgeService.updateKnowledge(knowledgeId, knowledgeDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 지식 삭제
    @DeleteMapping("/{knowledge_id}")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable("knowledge_id") UUID knowledgeId) {
        if (knowledgeService.deleteKnowledge(knowledgeId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
