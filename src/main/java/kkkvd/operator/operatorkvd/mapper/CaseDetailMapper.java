package kkkvd.operator.operatorkvd.mapper;

import kkkvd.operator.operatorkvd.dto.CaseDetailDto;
import kkkvd.operator.operatorkvd.dto.RefDto;
import kkkvd.operator.operatorkvd.entities.DetectionCase;

import java.util.List;

public final class CaseDetailMapper {
    private CaseDetailMapper() {}

    public static CaseDetailDto toDetailDto(DetectionCase dc, List<RefDto> labTests) {
        if (dc == null) {
            return null;
        }

        return CaseDetailDto.builder()
                .id(dc.getId())
                .patientId(dc.getPatient().getId())
                .diagnosisDate(dc.getDiagnosisDate())
                .createdAt(dc.getCreatedAt())
                .diagnosis(RefDtoMapper.toRef(dc.getDiagnosis()))
                .doctor(RefDtoMapper.toDoctorRef(dc.getDoctor()))
                .place(RefDtoMapper.toRef(dc.getPlace()))
                .profile(RefDtoMapper.toRef(dc.getProfile()))
                .inspection(RefDtoMapper.toRef(dc.getInspection()))
                .transfer(RefDtoMapper.toRef(dc.getTransfer()))
                .state(RefDtoMapper.toRef(dc.getState()))
                .citizenCategory(RefDtoMapper.toRef(dc.getCitizenCategory()))
                .citizenType(RefDtoMapper.toRef(dc.getCitizenType()))
                .socialGroup(RefDtoMapper.toRef(dc.getSocialGroup()))
                .isContact(dc.getIsContact())
                .labTests(labTests)
                .createdByUsername(dc.getCreatedBy())
                .build();
    }
}
