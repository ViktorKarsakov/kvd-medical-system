package kkkvd.operator.operatorkvd.controller;

import jakarta.validation.Valid;
import kkkvd.operator.operatorkvd.dto.*;
import kkkvd.operator.operatorkvd.entities.DetectionCase;
import kkkvd.operator.operatorkvd.service.AuditLogService;
import kkkvd.operator.operatorkvd.service.DetectionCaseService;
import kkkvd.operator.operatorkvd.service.SearchExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detection-cases")
@RequiredArgsConstructor
public class DetectionController {
    private final DetectionCaseService detectionCaseService;
    private final AuditLogService auditLogService;
    private final SearchExportService searchExportService;

    @PostMapping
    public ResponseEntity<DetectionCaseResponse> create(@Valid @RequestBody CreateDetectionCaseRequest request, Authentication authentication) {
        DetectionCase saved = detectionCaseService.create(request, authentication.getName());
        DetectionCaseResponse response = detectionCaseService.toResponse(saved);

        //записываем кто добавил случай и для какого пациента
        auditLogService.log("CREATE", "CASE", saved.getId(), "Добавлен случай: " + request.getLastName() + " " + request.getFirstName()
                + ", диагноз ID=" + request.getDiagnosisId(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<PatientSearchResult>> search(@Valid @RequestBody PatientSearchRequest request) {
        Page<PatientSearchResult> results = detectionCaseService.search(request);
        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DetectionCaseResponse>> getByPatientId(@PathVariable("patientId") Long patientId) {
        List<DetectionCase> result = detectionCaseService.getByPatientId(patientId);
        List<DetectionCaseResponse> responses = result.stream()
                .map(detectionCaseService::toResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseDetailDto> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(detectionCaseService.getCaseById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCase(@PathVariable Long id, Authentication authentication) {
        detectionCaseService.deleteCase(id);

        auditLogService.log("DELETE", "CASE", id, "Удалён случай #" + id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CaseDetailDto> updateCase(@PathVariable Long id, @Valid @RequestBody CreateCaseForPatientRequest request, Authentication authentication) {
        CaseDetailDto result = detectionCaseService.updateCase(id, request);

        auditLogService.log("UPDATE", "CASE", id, "Обновлён случай #" + id, authentication.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportSearch(@RequestBody PatientSearchRequest request) {
        try {
            // Получаем все результаты без пагинации
            List<PatientSearchResult> results = detectionCaseService.searchForExport(request);

            // Генерируем Excel
            byte[] excelBytes = searchExportService.exportToExcel(results);

            // Возвращаем файл с правильными заголовками
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "search-results.xlsx");

            return ResponseEntity.ok().headers(headers).body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
