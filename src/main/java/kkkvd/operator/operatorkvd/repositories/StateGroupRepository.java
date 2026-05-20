package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.StateGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateGroupRepository extends JpaRepository<StateGroup, Long> {
}
