package kkkvd.operator.operatorkvd.service;

import kkkvd.operator.operatorkvd.entities.AuditLog;
import kkkvd.operator.operatorkvd.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

//Сервис для журнала действий
@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    //Записать действие в журнал
    public void log(String action, String entityType, Long entityId, String details, String username) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        entry.setUsername(username);
        auditLogRepository.save(entry);
    }

    //Получить записи журнала с пагинацией и опциональными фильтрами
    public Page<AuditLog> getLogs(String entityType, String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Приоритет фильтров: entityType > username > без фильтра
        if(entityType != null && !entityType.isBlank()) {
            return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable);
        }
        if(username != null && !username.isBlank()) {
            return auditLogRepository.findByUsernameOrderByCreatedAtDesc(username, pageable);
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
