package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.DetectionCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetectionCaseRepository extends JpaRepository<DetectionCase, Long>, JpaSpecificationExecutor<DetectionCase> {

    List<DetectionCase> findByPatientId(Long patientId);

    List<DetectionCase> findByDiagnosisId(Long diagnosisId);

    List<DetectionCase> findByStateId(Long stateId);

    List<DetectionCase> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<DetectionCase> findByPatientIdOrderByDiagnosisDateDesc(Long patientId);

    long countByPatientId(Long patientId);

    long countByDoctorId(Long doctorId);

    long countByDiagnosisId(Long diagnosisId);

    long countByPlaceId(Long placeId);

    long countByProfileId(Long profileId);

    long countByInspectionId(Long inspectionId);

    long countByTransferId(Long transferId);

    long countByStateId(Long stateId);

    long countByCitizenCategoryId(Long citizenCategoryId);

    long countByCitizenTypeId(Long citizenTypeId);

    long countBySocialGroupId(Long socialGroupId);

    @Query("SELECT dc FROM DetectionCase dc " +
            "JOIN FETCH dc.patient p " +
            "JOIN FETCH p.gender " +
            "JOIN FETCH dc.diagnosis d " +
            "JOIN FETCH d.diagnosisGroup " +
            "JOIN FETCH dc.state st " +
            "JOIN FETCH st.stateGroup " +
            "JOIN FETCH dc.doctor " +
            "JOIN FETCH dc.place " +
            "JOIN FETCH dc.profile " +
            "JOIN FETCH dc.inspection " +
            "JOIN FETCH dc.transfer " +
            "JOIN FETCH dc.citizenCategory " +
            "JOIN FETCH dc.citizenType " +
            "JOIN FETCH dc.socialGroup " +
            "WHERE dc.diagnosisDate BETWEEN :dateFrom AND :dateTo " +
            "AND dc.state.id IN :stateIds")
    List<DetectionCase> findAllForReports(
            @Param("dateFrom")LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("stateIds") List<Long> stateIds);

    long countByDiagnosisDateBetween(LocalDate from, LocalDate to);

    List<DetectionCase> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT dg.name, COUNT(dc) FROM DetectionCase dc " +
            "JOIN dc.diagnosis d JOIN d.diagnosisGroup dg " +
            "WHERE dc.diagnosisDate BETWEEN :dateFrom AND :dateTo " +
            "GROUP BY dg.name ORDER BY COUNT(dc) DESC")
    List<Object[]> countByDiagnosisGroupBetween(
            @Param("dateFrom") LocalDate from,
            @Param("dateTo") LocalDate to);

    @Query("SELECT MONTH(dc.diagnosisDate), COUNT(dc) FROM DetectionCase dc " +
            "WHERE dc.diagnosisDate BETWEEN :dateFrom AND :dateTo " +
            "GROUP BY MONTH(dc.diagnosisDate) " +
            "ORDER BY MONTH(dc.diagnosisDate)")
    List<Object[]> countByMonthBetween(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);
}
