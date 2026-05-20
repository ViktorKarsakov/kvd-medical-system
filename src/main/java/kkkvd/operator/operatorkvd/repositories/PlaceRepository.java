package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
}
