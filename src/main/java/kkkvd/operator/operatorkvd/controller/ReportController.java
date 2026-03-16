package kkkvd.operator.operatorkvd.controller;

import kkkvd.operator.operatorkvd.dto.ReportRequest;
import kkkvd.operator.operatorkvd.service.ExcelReportService;
import kkkvd.operator.operatorkvd.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ExcelReportService excelReportService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportRequest request) throws IOException {
        //Формируем период для заголовка Excel
        String period = request.getDateFrom() + " - " + request.getDateTo();
        //готовый XLSX-файл
        byte[] file;
        // Имя файла для скачивания
        String fileName;

        //Определяем, какой отчёт нужен, по полю reportType из фронтенда
        switch (request.getReportType()) {
            //Отчёты 1-4: Структура заболеваемости
            case "structure" -> {
                String groupName = getGroupName(request.getDiagnosisGroupId());
                String title = "Структура заболеваемости: " + groupName + " за " + period;

                // ReportService считает данные (районы, возрасты, село)
                List<Map<String, Object>> data = reportService.generateStructureReport(request);

                //ExcelReportService превращает данные в XLSX
                file = excelReportService.exportStructureReport(data, title);
                fileName = "Структура_" + groupName + ".xlsx";
            }

            //Отчёт 5: Показатели заболеваемости
            //Не привязан к конкретной группе диагнозов — показывает ВСЕ группы в столбцах
            case "indicators" -> {
                String title = "Показатели заболеваемости за " + period;

                List<Map<String, Object>> data = reportService.generateIndicatorsReport(request);

                file = excelReportService.exportIndicatorsReport(data, title);
                fileName = "Показатели_заболеваемости.xlsx";
            }

            //Отчёт 6: ИППП на 100 тыс. населения
            //Использует данные из таблицы Population для расчёта показателей
            case "per100k" -> {
                String title = "ИППП на 100 тыс. населения за " + period;

                List<Map<String, Object>> data = reportService.generateIpppPer100kReport(request);

                file = excelReportService.exportIpppPer100kReport(data, title);
                fileName = "ИППП_на_100тыс.xlsx";
            }

            //Отчёт 7: Сведения о заболеваниях ИППП
            //Разбивка по диагнозам, полу и возрастным группам
            case "ippp_details" -> {
                String title = "Сведения о заболеваниях ИППП за " + period;

                List<Map<String, Object>> data = reportService.generateIpppDetailsReport(request);

                file = excelReportService.exportIpppDetailReport(data, title);
                fileName = "Сведения_ИППП.xlsx";
            }

            //Отчёт 8: Список больных по врачам и району
            //Список пациентов с ФИО, датой рождения, диагнозом, районом, врачом
            case "doctor_patients" -> {
                String groupName = getGroupName(request.getDiagnosisGroupId());
                String title = "Список больных: " + groupName + " за " + period;

                List<Map<String, Object>> data = reportService.generateDoctorPatientsReport(request);

                file = excelReportService.exportDoctorPatientReport(data, title);
                fileName = "Список_больных_" + groupName + ".xlsx";
            }

            //Отчёт 9: Подробный отчёт по заболеванию
            // Несколько секций (место выявления, соц. группа, район и т.д.).
            // Для сифилиса — столбцы по МКБ-кодам, для остальных — муж/жен/всего/%
            case "detailed" -> {
                String groupName = getGroupName(request.getDiagnosisGroupId());
                String title = "Подробный отчёт: " + groupName + " за " + period;

                //Данные — Map секций (а не List строк, как в других отчётах)
                Map<String, List<Map<String, Object>>> sections = reportService.generateDetailedReport(request);
                // isSyphilis нужен ExcelReportService для возможных будущих настроек
                boolean isSyphilis = reportService.isSyphilisGroup(request.getDiagnosisGroupId());

                file = excelReportService.exportDetailReport(sections, title, isSyphilis);
                fileName = "Подробный_" + groupName + ".xlsx";
            }

            // Неизвестный тип отчёта
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok()
                //MIME-тип для XLSX-файлов (Excel 2007+)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                // Content-Disposition: attachment — говорит браузеру "скачай файл, не открывай"
                // filename* — кодировка UTF-8 для русских букв в имени файла
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                .body(file);
    }

    //Название группы диагнозов по ID — для заголовка Excel-файла
    private String getGroupName(Long groupId) {
        if (groupId == null) {
            return "Все";
        }
        return reportService.getDiagnosisGroupName(groupId);
    }
}
