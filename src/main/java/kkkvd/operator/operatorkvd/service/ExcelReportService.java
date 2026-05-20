package kkkvd.operator.operatorkvd.service;

import kkkvd.operator.operatorkvd.util.ExcelStyleHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExcelReportService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Map<String, String> HEADER_LABELS = Map.ofEntries(
            //структура заболеваемости (отчеты 1 - 4)
            Map.entry("district", "Район"),
            Map.entry("total", "Всего"),
            Map.entry("under17", "До 17 лет"),
            Map.entry("under14", "До 14 лет"),
            Map.entry("under1", "До 1 года"),
            Map.entry("age1to2", "1-2 года"),
            Map.entry("age3to6", "3-6 лет"),
            Map.entry("age3to6ddu", "3-6 ДДУ"),
            Map.entry("rural", "Село"),
            Map.entry("under17rural", "До 17 (село)"),
            //сведения ИППП (отчет 7)
            Map.entry("diagnosis", "Диагноз"),
            Map.entry("gender", "Пол"),
            Map.entry("count", "Кол-во"),
            Map.entry("age0_1", "0-1 год"),
            Map.entry("age2_14", "2-14 лет"),
            Map.entry("age15_17", "15-17 лет"),
            Map.entry("age18_29", "18-29 лет"),
            Map.entry("age30_39", "30-39 лет"),
            Map.entry("age40plus", "40 и старше"),
            Map.entry("rural0_1", "Село 0-1"),
            Map.entry("rural2_14", "Село 2-14"),
            Map.entry("rural15_17", "Село 15-17"),
            //список по врачам (отчёт 8)
            Map.entry("patient", "Пациент"),
            Map.entry("birthDate", "Дата рождения"),
            Map.entry("doctor", "Врач"),
            //подробный отчет (отчёт 9)
            Map.entry("name", "Наименование"),
            Map.entry("male", "Муж"),
            Map.entry("female", "Жен"),
            Map.entry("percent", "%"),
            Map.entry("per100k", "На 100 тыс.")
    );

    //Отчёты 1-4: Структура заболеваемости (Сифилис / Гонорея / Микроспория / Чесотка)
    public byte[] exportStructureReport(List<Map<String, Object>> data, String title) throws IOException {
        return exportSingleTable(title, data);
    }

    //Отчёт 5: Показатели заболеваемости
    public byte[] exportIndicatorsReport(List<Map<String, Object>> data, String title) throws IOException {
        return exportSingleTable(title, data);
    }

    //Отчёт 6: ИППП на 100 тыс. населения
    public byte[] exportIpppPer100kReport(List<Map<String, Object>> data, String title) throws IOException {
        return exportSingleTable(title, data);
    }

    //Отчёт 7: Сведения о заболеваниях ИППП
    public byte[] exportIpppDetailReport(List<Map<String, Object>> data, String title) throws IOException {
        return exportSingleTable(title, data);
    }

    //Отчёт 8: Список больных по врачам и району
    public byte[] exportDoctorPatientReport(List<Map<String, Object>> data, String title) throws IOException {
        return exportSingleTable(title, data);
    }

    //Отчёт 9: Подробный отчёт по заболеванию
    public byte[] exportDetailReport(Map<String, List<Map<String, Object>>> sections, String title, boolean isSyphilis) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Лист1");
            ExcelStyleHelper styles = new ExcelStyleHelper(wb);
            int rowNum = 0;
            // 1) Большой заголовок отчёта (объединяем ячейки на 0..15)
            rowNum = writeBigTitle(sheet, rowNum, title, styles);
            // 2) Пустая строка-разделитель
            rowNum++;

            // 3) Проходим по всем секциям и пишем каждую как отдельную таблицу
            for (Map.Entry<String, List<Map<String, Object>>> entry : sections.entrySet()) {
                String sectionName = entry.getKey();
                List<Map<String, Object>> sectionData = entry.getValue();

                // Пропускаем пустые секции (например, если не было случаев)
                if (sectionData == null || sectionData.isEmpty()) {
                    continue;
                }

                // 3.1) Заголовок секции
                rowNum = writeSectionTitle(sheet, rowNum, sectionName, styles);
                // 3.2) Таблица секции (шапка + строки данных)
                rowNum = writeTable(sheet, rowNum, sectionData, styles);
                // 3.3) Пустая строка между секциями
                rowNum++;
            }
            //4) Автоширина столбцов
            autoSizeSafe(sheet, 25);
            return toBytes(wb);
        }
    }

    private byte[] exportSingleTable(String title, List<Map<String, Object>> data) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Лист1");
            // Создаём стили один раз для этого Workbook
            ExcelStyleHelper styles = new ExcelStyleHelper(wb);
            int rowNum = 0;
            // 1) Заголовок
            rowNum = writeBigTitle(sheet, rowNum, title, styles);
            // 2) Пустая строка
            rowNum++;
            // 3) Таблица (шапка + данные)
            rowNum = writeTable(sheet, rowNum, data, styles);
            // 4) Автоширина столбцов по содержимому
            autoSizeByData(sheet, data);
            return toBytes(wb);
        }
    }

    //Пишет большой заголовок отчёта и объединяет ячейки 0..15
    private int writeBigTitle(Sheet sheet, int rowNum, String title, ExcelStyleHelper styles) throws IOException {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(styles.getTitleStyle());
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 15));
        return rowNum + 1;
    }

    //Пишет заголовок секции (для подробного отчёта)
    private int writeSectionTitle(Sheet sheet, int rowNum, String sectionName, ExcelStyleHelper styles) throws IOException {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(sectionName);
        cell.setCellStyle(styles.getSectionStyle());
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 15));
        return rowNum + 1;
    }

    //Запись таблицы (шапка + строки данных)
    private int writeTable(Sheet sheet, int rowNum, List<Map<String, Object>> data, ExcelStyleHelper styles) throws IOException {
        if (data == null || data.isEmpty()) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue("Нет данных");
            return rowNum + 1;
        }
        //Определяем столбцы по ключам первой строки
        List<String> keys = new ArrayList<>(data.get(0).keySet());
        //Шапка, переводим технические ключи в русские заголовки
        Row headerRow = sheet.createRow(rowNum++);
        for (int col = 0; col < keys.size(); col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(translateHeader(keys.get(col)));
            cell.setCellStyle(styles.getHeaderStyle());
        }
        //Пишем строки данных
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            //Определяем, является ли строка итоговой (ИТОГО, Итого по Красноярску и т.д.)
            boolean isHighlight = isHighlightRow(rowData);

            for (int col = 0; col < keys.size(); col++) {
                String key = keys.get(col);
                Object value = rowData.get(key);
                Cell cell = row.createCell(col);
                // Записываем значение в ячейку (число, дата, строка или пусто)
                writeCellValue(cell, value);
                // Итоговые строки делаем жирными
                cell.setCellStyle(isHighlight ? styles.getItogoStyle() : styles.getNormalStyle());
            }
        }
        return rowNum;
    }

    //Переводит технический ключ Map в русский заголовок для Excel
    private String translateHeader(String key) {
        return HEADER_LABELS.getOrDefault(key, key);
    }

    //Определяет, нужно ли выделять строку жирным шрифтом
    private boolean isHighlightRow(Map<String, Object> rowData) {
        for (Object value : rowData.values()) {
            if (value instanceof String text) {
                String trimmed = text.trim();
                if (trimmed.equalsIgnoreCase("ИТОГО")
                        || trimmed.startsWith("Итого по")
                        || trimmed.startsWith("Всего:")) {
                    return true;
                }
            }
        }
        return false;
    }

    //Записывает значение в ячейку Excel по его типу
    private void writeCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof LocalDate date) {
            cell.setCellValue(DATE_FMT.format(date));
            return;
        }
        cell.setCellValue(value.toString());
    }

    //Автоширина для отчётов с одной таблицей
    private void autoSizeByData(Sheet sheet, List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int colCount = data.get(0).size();

        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    //Безопасная автоширина для подробного отчёта
    private void autoSizeSafe(Sheet sheet, int maxCol) {
        for (int i = 0; i <= maxCol; i++) {
            try {
                sheet.autoSizeColumn(i);
            } catch (Exception ignored) {}
        }
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }

}
