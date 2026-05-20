package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.SocialGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialGroupRepository extends JpaRepository<SocialGroup, Long> {
}
