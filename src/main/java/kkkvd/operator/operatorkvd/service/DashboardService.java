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

    // Возвращает список ИППП-групп с количеством случаев за текущий год.
    public List<Map<String, Object>> getIpppDistribution(int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        List<Object[]> rows = caseRepository.countIpppByGroupBetween(from, to);
        return rows.stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", row[0]);
                    item.put("code", row[1]);
                    item.put("count", row[2]);
                    return item;
                })
                .toList();
    }

    // Возвращает массив из 12 чисел — случаев по месяцам за текущий год для конкретной ИППП-группы.
    public int[] getIpppMonthlyCases(String groupCode, int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        List<Object[]> rows = caseRepository.countByMonthForGroupBetween(from, to, groupCode);
        int[] monthly = new int[12];
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();
            monthly[month - 1] = count;
        }
        return monthly;
    }

    public List<Integer> getAvailableYears() {
        return caseRepository.findDistinctYears();
    }
}
