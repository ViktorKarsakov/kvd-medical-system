package kkkvd.operator.operatorkvd.mapper;

import kkkvd.operator.operatorkvd.dto.RefDto;
import kkkvd.operator.operatorkvd.entities.Doctor;
import kkkvd.operator.operatorkvd.entities.NamedDictionary;
import kkkvd.operator.operatorkvd.util.DoctorNameFormatter;

public final class RefDtoMapper {
    private RefDtoMapper() {}

    public static RefDto toRef(NamedDictionary dictionary) {
        if (dictionary == null) {
            return null;
        }
        return RefDto.builder()
                .id(dictionary.getId())
                .name(dictionary.getName())
                .build();
    }

    public static RefDto toDoctorRef(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        return RefDto.builder()
                .id(doctor.getId())
                .name(DoctorNameFormatter.formatShort(doctor))
                .build();
    }
}
