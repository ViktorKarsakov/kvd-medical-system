package kkkvd.operator.operatorkvd.repositories;

import kkkvd.operator.operatorkvd.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    //Все записи, от новых к старым (для общего просмотра журнала)
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    //Фильтр по типу сущности: "покажи только действия с пациентами"
    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);

    //Фильтр по пользователю
    Page<AuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
}
