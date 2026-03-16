package kkkvd.operator.operatorkvd.service;

import kkkvd.operator.operatorkvd.dto.PatientSearchResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

//Сервис для экспорта результатов поиска пациентов в Excel
@Service
public class SearchExportService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    //Генерирует Excel-файл из списка результатов поиска
    public byte[] exportToExcel(List<PatientSearchResult> results) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Результаты поиска");

            //Стиль заголовков
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            //Заголовки
            String[] headers = {
                    "Фамилия", "Имя", "Отчество", "Пол",
                    "Дата рождения", "Район", "Диагноз",
                    "Дата диагноза", "Врач", "Дата учёта"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            //Данные
            int rowNum = 1;
            for (PatientSearchResult r : results) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(safe(r.getLastName()));
                row.createCell(1).setCellValue(safe(r.getFirstName()));
                row.createCell(2).setCellValue(safe(r.getMiddleName()));
                row.createCell(3).setCellValue(safe(r.getGenderName()));
                row.createCell(4).setCellValue(safe(r.getBirthDate() != null ? r.getBirthDate().format(DATE_FMT) : ""));
                row.createCell(5).setCellValue(safe(r.getStateName()));
                row.createCell(6).setCellValue(safe(r.getDiagnosisName()));
                row.createCell(7).setCellValue(safe(r.getDiagnosisDate() != null ? r.getDiagnosisDate().format(DATE_FMT) : ""));
                row.createCell(8).setCellValue(safe(r.getDoctorName()));
                row.createCell(9).setCellValue(safe(r.getCreatedAt() != null ? r.getCreatedAt().format(DATETIME_FMT) : ""));
            }

            //Авторазмер колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            //Записываем в byte[]
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    //Защита от null: если значение null — возвращаем пустую строку
    private String safe(String value) {
        return value != null ? value : "";
    }
}
