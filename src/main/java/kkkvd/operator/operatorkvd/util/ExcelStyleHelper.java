package kkkvd.operator.operatorkvd.util;

import org.apache.poi.ss.usermodel.*;

public class ExcelStyleHelper {
    //Стиль для большого заголовка отчёта ("Структура заболеваемости сифилисом за 2025 г.")
    private final CellStyle titleStyle;
    // Стиль для заголовка секции в подробном отчёте ("Место выявления", "Социальная группа")
    private final CellStyle sectionStyle;
    // Стиль для шапки таблицы (жирный текст, по центру, с рамками)
    private final CellStyle headerStyle;
    // Стиль для обычных ячеек данных (с рамками)
    private final CellStyle normalStyle;
    // Стиль для строки ИТОГО / "Итого по Красноярску" (жирный, с рамками)
    private final CellStyle itogoStyle;

    public ExcelStyleHelper(Workbook wb) {
        // Обычный шрифт (Arial 10, не жирный)
        Font normalFont = wb.createFont();
        normalFont.setFontName("Arial");
        normalFont.setFontHeightInPoints((short) 10);

        // Жирный шрифт (Arial 10, жирный) — для шапок, ИТОГО
        Font boldFont = wb.createFont();
        boldFont.setFontName("Arial");
        boldFont.setFontHeightInPoints((short) 10);
        boldFont.setBold(true);

        // Крупный жирный шрифт (Arial 12) — для заголовка всего отчёта
        Font titleFont = wb.createFont();
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 12);
        titleFont.setBold(true);

        //Стили
        // Заголовок отчёта: крупный, по центру, без рамок
        titleStyle = wb.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setWrapText(true);

        // Заголовок секции: жирный, по левому краю, без рамок
        sectionStyle = wb.createCellStyle();
        sectionStyle.setFont(boldFont);
        sectionStyle.setAlignment(HorizontalAlignment.LEFT);
        sectionStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        sectionStyle.setWrapText(true);

        // Шапка таблицы: жирный, по центру, с рамками
        headerStyle = wb.createCellStyle();
        headerStyle.setFont(boldFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);
        addBorders(headerStyle);

        // Обычная ячейка: обычный шрифт, с рамками
        normalStyle = wb.createCellStyle();
        normalStyle.setFont(normalFont);
        normalStyle.setVerticalAlignment(VerticalAlignment.TOP);
        normalStyle.setWrapText(true);
        addBorders(normalStyle);

        // Строка ИТОГО: жирный шрифт, с рамками (всё как normal, но bold)
        itogoStyle = wb.createCellStyle();
        itogoStyle.setFont(boldFont);
        itogoStyle.setVerticalAlignment(VerticalAlignment.TOP);
        itogoStyle.setWrapText(true);
        addBorders(itogoStyle);
    }

    public CellStyle getTitleStyle() {
        return titleStyle;
    }

    public CellStyle getSectionStyle() {
        return sectionStyle;
    }

    public CellStyle getHeaderStyle() {
        return headerStyle;
    }

    public CellStyle getNormalStyle() {
        return normalStyle;
    }

    public CellStyle getItogoStyle() {
        return itogoStyle;
    }

    //тонкие рамки со всех четырёх сторон ячейки
    private void addBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
