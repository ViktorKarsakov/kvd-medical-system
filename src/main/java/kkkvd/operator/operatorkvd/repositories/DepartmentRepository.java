package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    long countByBranchId(Long branchId);
}
