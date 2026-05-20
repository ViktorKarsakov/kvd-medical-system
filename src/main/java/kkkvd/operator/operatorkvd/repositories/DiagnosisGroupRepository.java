package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.DiagnosisGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosisGroupRepository extends JpaRepository<DiagnosisGroup, Long> {

    Optional<DiagnosisGroup> findByCode(String code);
}
