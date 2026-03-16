package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    List<Diagnosis> findByDiagnosisGroupId(Long diagnosisGroupId);

    List<Diagnosis> findAllByOrderByNameAsc();

    long countByDiagnosisGroupId(Long diagnosisGroupId);

    List<Diagnosis> findByDiagnosisGroupIdOrderByNameAsc(Long groupId);
}
