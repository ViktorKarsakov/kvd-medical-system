package kkkvd.operator.operatorkvd.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequest {

    private String reportType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String regionFilter;
    private Long stateId;
    private Long diagnosisGroupId;
}
