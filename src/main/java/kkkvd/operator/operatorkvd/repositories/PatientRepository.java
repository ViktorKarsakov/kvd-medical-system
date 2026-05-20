package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> , JpaSpecificationExecutor<Patient> {
    Optional<Patient> findByLastNameAndFirstNameAndMiddleNameAndBirthDate(
            String lastName, String firstName, String middleName,  LocalDate birthDate);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT(:lastName, '%')) AND " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT(:firstName, '%')) AND " +
            "(:middleName IS NULL OR LOWER(p.middleName) LIKE LOWER(CONCAT(:middleName, '%')))")
    List<Patient> searchByName(
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("middleName") String middleName);

    @Query("SELECT p FROM Patient p WHERE " +
            "p.lastName = :lastName AND " +
            "p.firstName = :firstName AND " +
            "p.birthDate = :birthDate AND " +
            "(p.middleName = :middleName OR (p.middleName IS NULL AND :middleName IS NULL))")
    Optional<Patient> findByFullNameAndBirthDate(
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("middleName") String middleName,
            @Param("birthDate") LocalDate birthDate
    );

    boolean existsByLastNameAndFirstNameAndMiddleNameAndBirthDate(
            String lastName, String firstName, String middleName, LocalDate birthDate);

    long countByGenderId(Long genderId);
}
