package kkkvd.operator.operatorkvd.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateCaseForPatientRequest {
    //Пациент уже есть - добавить еще один случай заболевания

    @NotNull(message = "Категория проживания обязательна")
    private Long citizenCategoryId;
    @NotNull(message = "Тип населённого пункта обязателен")
    private Long citizenTypeId;
    @NotNull(message = "Район обязателен")
    private Long stateId;
    @NotNull(message = "Социальная группа обязательна")
    private Long socialGroupId;
    @NotNull(message = "Диагноз обязателен")
    private Long diagnosisId;
    @NotNull(message = "Дата диагноза обязательна")
    private LocalDate diagnosisDate;
    @NotNull(message = "Врач обязателен")
    private Long doctorId;
    @NotNull(message = "Место выявления обязательно")
    private Long placeId;
    @NotNull(message = "Профиль обязателен")
    private Long profileId;
    @NotNull(message = "Тип осмотра обязателен")
    private Long inspectionId;
    @NotNull(message = "Путь передачи обязателен")
    private Long transferId;
    private Boolean isContact;
    private Set<Long> labTestIds;
}
