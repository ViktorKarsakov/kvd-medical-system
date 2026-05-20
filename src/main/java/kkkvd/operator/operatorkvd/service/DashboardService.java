package kkkvd.operator.operatorkvd.service;

import kkkvd.operator.operatorkvd.repositories.DetectionCaseRepository;
import kkkvd.operator.operatorkvd.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Сервис для главной страницы (дашборда)
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final PatientRepository patientRepository;
    private final DetectionCaseRepository caseRepository;

    //Собирает всю статистику для дашборда одним вызовом
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        //Общие счётчики
        stats.put("totalPatients", patientRepository.count());
        stats.put("totalCases", caseRepository.count());
        //Случаи за текущий месяц
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        stats.put("casesThisMonth", caseRepository.countByDiagnosisDateBetween(monthStart, now));
        //Случаи за текущий год
        LocalDate yearStart = now.withDayOfYear(1);
        stats.put("casesThisYear", caseRepository.countByDiagnosisDateBetween(yearStart, now));

        //Распределение по группам диагнозов (за текущий год)
        List<Object[]> byGroup = caseRepository.countByDiagnosisGroupBetween(yearStart, now);
        List<Map<String, Object>> distribution = byGroup.stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", row[0]);
                    item.put("count", row[1]);
                    return item;
                })
                .toList();
        stats.put("diagnosisDistribution", distribution);

        //Динамика по месяцам (для графика)
        List<Object[]> byMonth = caseRepository.countByMonthBetween(yearStart, now);
        int[] monthlyData = new int[12];
        for (Object[] row : byMonth) {
            int month = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();
            monthlyData[month - 1] = count;
        }
        stats.put("monthlyCases", monthlyData);

        return stats;
    }
}
