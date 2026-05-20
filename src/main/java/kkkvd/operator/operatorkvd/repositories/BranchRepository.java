package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
}
