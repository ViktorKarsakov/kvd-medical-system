package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    List<State> findByStateGroupId(Long stateGroupId);

    List<State> findAllByOrderByStateGroupIdAscNameAsc();

    long countByStateGroupId(Long stateGroupId);

    List<State> findByStateGroupCode(String code);
}
