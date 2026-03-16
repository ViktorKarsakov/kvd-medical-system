package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartmentId(Long departmentId);

    List<Doctor> findAllByOrderByLastNameAscFirstNameAsc();

    long countByDepartmentId(Long departmentId);
}
