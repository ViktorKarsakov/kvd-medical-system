package kkkvd.operator.operatorkvd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseDetailDto {
    //Карточка случая пациента

    private Long id;
    private Long patientId;
    private LocalDate diagnosisDate;
    private LocalDateTime createdAt;
    private RefDto diagnosis;
    private RefDto doctor;
    private RefDto place;
    private RefDto profile;
    private RefDto inspection;
    private RefDto transfer;
    private RefDto state;
    private RefDto citizenCategory;
    private RefDto citizenType;
    private RefDto socialGroup;
    private Boolean isContact;
    private List<RefDto> labTests;
    private String createdByUsername;
}
