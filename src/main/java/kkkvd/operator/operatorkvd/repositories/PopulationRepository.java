package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Population;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PopulationRepository extends JpaRepository<Population, Long> {

    Optional<Population> findByStateIdAndYear(Long stateId, Integer year);

    @Query("SELECT p FROM Population p " +
            "JOIN FETCH p.state WHERE p.year = :year")
    List<Population> findByYear(Integer year);

    @Query("SELECT p FROM Population p " +
            "JOIN FETCH p.state s " +
            "ORDER BY p.year DESC, s.name ASC")
    List<Population> findAllByOrderByYearDescStateNameAsc();

    // Находит последний год, за который есть данные о населении.
    @Query("SELECT MAX(p.year) FROM Population p")
    Optional<Integer> findLatestYear();
}
