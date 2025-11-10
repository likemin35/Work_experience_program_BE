package com.experience_program.be.controller;

import com.experience_program.be.dto.DashboardSummaryDto;
import com.experience_program.be.entity.Campaign;
import com.experience_program.be.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 대시보드 요약 조회
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    // 최근 활동 조회
    @GetMapping("/recent-activity")
    public ResponseEntity<List<Campaign>> getRecentActivity() {
        List<Campaign> recentCampaigns = dashboardService.getRecentActivity();
        return ResponseEntity.ok(recentCampaigns);
    }
}
