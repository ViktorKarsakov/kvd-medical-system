package kkkvd.operator.operatorkvd.controller;

import jakarta.validation.Valid;
import kkkvd.operator.operatorkvd.dto.CreateCaseForPatientRequest;
import kkkvd.operator.operatorkvd.dto.DetectionCaseResponse;
import kkkvd.operator.operatorkvd.dto.PatientDetailResponse;
import kkkvd.operator.operatorkvd.dto.UpdatePatientRequest;
import kkkvd.operator.operatorkvd.service.AuditLogService;
import kkkvd.operator.operatorkvd.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AuditLogService auditLogService;

    @GetMapping("/{id}")
    public ResponseEntity<PatientDetailResponse> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientWithCases(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDetailResponse> updatePatient(@PathVariable Long id, @Valid @RequestBody UpdatePatientRequest request, Authentication authentication) {
        PatientDetailResponse response = patientService.updatePatient(id, request);
        auditLogService.log("UPDATE", "PATIENT", id, "Обновлён пациент: " + request.getLastName()
                + " " + request.getFirstName(), authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id, Authentication authentication) {
        patientService.deletePatient(id);
        auditLogService.log("DELETE", "PATIENT", id, "Удалён пациент #" + id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cases")
    public ResponseEntity<DetectionCaseResponse> addCaseToPatient(@PathVariable Long id, @Valid @RequestBody CreateCaseForPatientRequest request, Authentication authentication) {
        DetectionCaseResponse response = patientService.addCaseToPatient(id, request, authentication.getName());
        auditLogService.log("CREATE", "CASE", response.getId(), "Добавлен случай к пациенту #" + id, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
