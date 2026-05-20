package kkkvd.operator.operatorkvd.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DetectionCaseResponse {

    private Long id;
    private Long patientId;
    private String patientFullName;
    private String diagnosisName;
    private LocalDate diagnosisDate;
    private LocalDateTime createdAt;
}
