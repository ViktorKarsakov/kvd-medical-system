package kkkvd.operator.operatorkvd.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detection_case_lab_tests")
@Getter
@Setter
@NoArgsConstructor
public class DetectionCaseLabTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_case_id", nullable = false)
    private DetectionCase detectionCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_type_id", nullable = false)
    private LaboratoryTestType laboratoryTestType;
}
