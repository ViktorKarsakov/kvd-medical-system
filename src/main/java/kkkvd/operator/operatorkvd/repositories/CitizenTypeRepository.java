package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.CitizenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitizenTypeRepository extends JpaRepository<CitizenType, Long> {
}
