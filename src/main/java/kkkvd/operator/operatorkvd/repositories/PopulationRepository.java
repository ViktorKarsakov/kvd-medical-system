package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Population;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PopulationRepository extends JpaRepository<Population, Long> {

    Optional<Population> findByStateIdAndYear(Long stateId, Integer year);

    List<Population> findByYear(Integer year);

    List<Population> findAllByOrderByYearDescStateNameAsc();
}
