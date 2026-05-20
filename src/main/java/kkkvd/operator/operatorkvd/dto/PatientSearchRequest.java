package kkkvd.operator.operatorkvd.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientSearchRequest {
    //Поиск, тут все фильтры и page, size


    private String lastName;
    private String firstName;
    private String middleName;
    private Long genderId;

    private Integer ageFrom;
    private Integer ageTo;

    private Long stateId;
    private Long diagnosisId;
    private Long diagnosisGroupId;
    private Long socialGroupId;
    private Long doctorId;

    private LocalDate createdFrom;
    private LocalDate createdTo;

    private Integer page = 0;
    private Integer size = 20;
}
