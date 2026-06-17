package kkkvd.operator.operatorkvd.controller;

import kkkvd.operator.operatorkvd.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

//Контроллер дашборда (главная страница)
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    // Распределение по ИППП-группам для виджета на главной странице
    @GetMapping("/ippp-distribution")
    public ResponseEntity<List<Map<String, Object>>> getIpppDistribution(@RequestParam int year) {
        return ResponseEntity.ok(dashboardService.getIpppDistribution(year));
    }

    // Динамика по месяцам для конкретной ИППП-группы.
    @GetMapping("/ippp-monthly")
    public ResponseEntity<int[]> getIpppMonthly(@RequestParam String groupCode, @RequestParam int year) {
        return ResponseEntity.ok(dashboardService.getIpppMonthlyCases(groupCode, year));
    }

    @GetMapping("/available-years")
    public ResponseEntity<List<Integer>> getAvailableYears() {
        return ResponseEntity.ok(dashboardService.getAvailableYears());
    }
}
