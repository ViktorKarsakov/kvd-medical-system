package kkkvd.operator.operatorkvd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDetailResponse {
    private Long id;

    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthDate;

    private RefDto gender;
    private String address;

    private List<CaseSummaryDto> cases;
}
