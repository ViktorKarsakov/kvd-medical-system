package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
