package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.LaboratoryTestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaboratoryTestTypeRepository extends JpaRepository<LaboratoryTestType, Long> {
}
