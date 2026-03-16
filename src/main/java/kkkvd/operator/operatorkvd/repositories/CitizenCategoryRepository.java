package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.CitizenCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitizenCategoryRepository extends JpaRepository<CitizenCategory, Long> {
}
