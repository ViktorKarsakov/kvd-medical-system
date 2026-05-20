package kkkvd.operator.operatorkvd.controller;

import kkkvd.operator.operatorkvd.service.AuditLogService;
import kkkvd.operator.operatorkvd.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Контроллер для создания бэкапа БД
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {
    private final BackupService backupService;
    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<byte[]> createBackup(Authentication authentication) {
        byte[] backup = backupService.createBackup();
        String fileName = backupService.generateFileName();

        //Записываем в журнал
        auditLogService.log("CREATE", "BACKUP", null, "Создан бэкап БД: " + fileName, authentication.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok().headers(headers).body(backup);
    }
}
