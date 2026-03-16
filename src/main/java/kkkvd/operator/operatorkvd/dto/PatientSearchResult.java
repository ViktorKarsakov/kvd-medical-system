package kkkvd.operator.operatorkvd.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PatientSearchResult {
    //Результаты поиска на форме Поиска в одну строку

    private Long patientId;
    private Long detectionCaseId;

    private String lastName;
    private String firstName;
    private String middleName;
    private String genderName;
    private LocalDate birthDate;

    private String stateName;
    private String diagnosisName;
    private LocalDate diagnosisDate;
    private String doctorName;
    private LocalDateTime createdAt;
}
