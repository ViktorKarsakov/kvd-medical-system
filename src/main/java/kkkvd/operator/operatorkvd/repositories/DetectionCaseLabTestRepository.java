package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.DetectionCaseLabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetectionCaseLabTestRepository extends JpaRepository<DetectionCaseLabTest, Long> {

    List<DetectionCaseLabTest> findByDetectionCaseId(Long detectionCaseId);

    void deleteByDetectionCaseId(Long detectionCaseId);

    long countByLaboratoryTestTypeId(Long laboratoryTestTypeId);
}
