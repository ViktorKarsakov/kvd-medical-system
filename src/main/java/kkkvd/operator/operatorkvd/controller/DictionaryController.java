package kkkvd.operator.operatorkvd.controller;

import kkkvd.operator.operatorkvd.entities.*;
import kkkvd.operator.operatorkvd.service.AuditLogService;
import kkkvd.operator.operatorkvd.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryService dictionaryService;
    private final AuditLogService auditLogService;

    //Вспомогательный метод — логирует действие со справочником
    private void logDict(String action, String dictName, Long id, String name, Authentication authentication){
        String actionRu;
        switch (action){
            case "CREATE": actionRu = "Добавлен";  break;
            case "UPDATE": actionRu = "Обновлен"; break;
            case "DELETE": actionRu = "Удален"; break;
            default: actionRu = action;
        }
        String details = actionRu + " " + dictName;
        if (name != null) {
            details += ": " + name;
        } else {
            details += " #" + id;
        }
        auditLogService.log(action, "DICTIONARY", id, details, authentication.getName());
    }

    //gender

    @GetMapping("/genders")
    public ResponseEntity<List<Gender>> getGenders() {
        return ResponseEntity.ok(dictionaryService.getAllGenders());
    }

    @PostMapping("/genders")
    public ResponseEntity<Gender> createGender(@RequestBody Gender gender, Authentication auth) {
        Gender saved = dictionaryService.createGender(gender);
        logDict("CREATE", "пол", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/genders/{id}")
    public ResponseEntity<Gender> updateGender(@PathVariable Long id, @RequestBody Gender gender, Authentication auth) {
        Gender updated = dictionaryService.updateGender(id, gender);
        logDict("UPDATE", "пол", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/genders/{id}")
    public ResponseEntity<Void> deleteGender(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "пол", id, null, auth);
        dictionaryService.deleteGender(id);
        return ResponseEntity.noContent().build();
    }


    //Diagnosis

    @GetMapping("/diagnoses")
    public ResponseEntity<List<Diagnosis>> getDiagnosis() {
        return ResponseEntity.ok(dictionaryService.getAllDiagnoses());
    }

    @PostMapping("/diagnoses")
    public ResponseEntity<Diagnosis> createDiagnosis(@RequestBody Diagnosis diagnosis, Authentication auth) {
        Diagnosis saved = dictionaryService.createDiagnosis(diagnosis);
        logDict("CREATE", "диагноз", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/diagnoses/{id}")
    public ResponseEntity<Diagnosis> updateDiagnosis(@PathVariable Long id, @RequestBody Diagnosis diagnosis, Authentication auth) {
        Diagnosis updated = dictionaryService.updateDiagnosis(id, diagnosis);
        logDict("UPDATE", "диагноз", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/diagnoses/{id}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "диагноз", id, null, auth);
        dictionaryService.deleteDiagnosis(id);
        return ResponseEntity.noContent().build();
    }

    //DiagnosisGroups

    @GetMapping("/diagnosis-groups")
    public ResponseEntity<List<DiagnosisGroup>> getDiagnosisGroups() {
        return ResponseEntity.ok(dictionaryService.getAllDiagnosisGroups());
    }

    @PostMapping("/diagnosis-groups")
    public ResponseEntity<DiagnosisGroup> createDiagnosisGroup(@RequestBody DiagnosisGroup diagnosisGroup, Authentication auth) {
        DiagnosisGroup saved = dictionaryService.createDiagnosisGroup(diagnosisGroup);
        logDict("CREATE", "группа диагнозов", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/diagnosis-groups/{id}")
    public ResponseEntity<DiagnosisGroup> updateDiagnosisGroup(@PathVariable Long id, @RequestBody DiagnosisGroup diagnosisGroup, Authentication auth) {
        DiagnosisGroup updated = dictionaryService.updateDiagnosisGroup(id, diagnosisGroup);
        logDict("UPDATE", "группа диагнозов", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/diagnosis-groups/{id}")
    public ResponseEntity<Void> deleteDiagnosisGroup(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "группа диагнозов", id, null, auth);
        dictionaryService.deleteDiagnosisGroup(id);
        return ResponseEntity.noContent().build();
    }

    //Doctor

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getDoctors() {
        return ResponseEntity.ok(dictionaryService.getAllDoctors());
    }

    @PostMapping("/doctors")
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor, Authentication auth) {
        Doctor saved = dictionaryService.createDoctor(doctor);
        logDict("CREATE", "врач", saved.getId(), saved.getLastName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor, Authentication auth) {
        Doctor updated = dictionaryService.updateDoctor(id, doctor);
        logDict("UPDATE", "врач", id, updated.getLastName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "врач", id, null, auth);
        dictionaryService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    //State

    @GetMapping("/states")
    public ResponseEntity<List<State>> getStates() {
        return ResponseEntity.ok(dictionaryService.getAllStates());
    }

    @PostMapping("/states")
    public ResponseEntity<State> createState(@RequestBody State state, Authentication auth) {
        State saved = dictionaryService.createState(state);
        logDict("CREATE", "район", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/states/{id}")
    public ResponseEntity<State> updateState(@PathVariable Long id, @RequestBody State state, Authentication auth) {
        State updated = dictionaryService.updateState(id, state);
        logDict("UPDATE", "район", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/states/{id}")
    public ResponseEntity<Void> deleteState(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "район", id, null, auth);
        dictionaryService.deleteState(id);
        return ResponseEntity.noContent().build();
    }

    //StateGroup

    @GetMapping("/state-groups")
    public ResponseEntity<List<StateGroup>> getStateGroups() {
        return ResponseEntity.ok(dictionaryService.getAllStateGroups());
    }

    @PostMapping("/state-groups")
    public ResponseEntity<StateGroup> createStateGroup(@RequestBody StateGroup stateGroup, Authentication auth) {
        StateGroup saved = dictionaryService.createStateGroup(stateGroup);
        logDict("CREATE", "группа районов", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/state-groups/{id}")
    public ResponseEntity<StateGroup> updateStateGroup(@PathVariable Long id, @RequestBody StateGroup stateGroup, Authentication auth) {
        StateGroup updated = dictionaryService.updateStateGroup(id, stateGroup);
        logDict("UPDATE", "группа районов", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/state-groups/{id}")
    public ResponseEntity<Void> deleteStateGroup(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "группа районов", id, null, auth);
        dictionaryService.deleteStateGroup(id);
        return ResponseEntity.noContent().build();
    }

    // Place

    @GetMapping("/places")
    public ResponseEntity<List<Place>> getPlaces() {
        return ResponseEntity.ok(dictionaryService.getAllPlaces());
    }

    @PostMapping("/places")
    public ResponseEntity<Place> createPlace(@RequestBody Place place, Authentication auth) {
        Place saved = dictionaryService.createPlace(place);
        logDict("CREATE", "место выявления", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/places/{id}")
    public ResponseEntity<Place> updatePlace(@PathVariable Long id, @RequestBody Place place, Authentication auth) {
        Place updated = dictionaryService.updatePlace(id, place);
        logDict("UPDATE", "место выявления", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/places/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "место выявления", id, null, auth);
        dictionaryService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }

    //Profile

    @GetMapping("/profiles")
    public ResponseEntity<List<Profile>> getProfiles() {
        return ResponseEntity.ok(dictionaryService.getAllProfiles());
    }

    @PostMapping("/profiles")
    public ResponseEntity<Profile> createProfile(@RequestBody Profile profile, Authentication auth) {
        Profile saved = dictionaryService.createProfile(profile);
        logDict("CREATE", "профиль", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/profiles/{id}")
    public ResponseEntity<Profile> updateProfile(@PathVariable Long id, @RequestBody Profile profile, Authentication auth) {
        Profile updated = dictionaryService.updateProfile(id, profile);
        logDict("UPDATE", "профиль", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/profiles/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "профиль", id, null, auth);
        dictionaryService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    //Inspection

    @GetMapping("/inspections")
    public ResponseEntity<List<Inspection>> getInspections() {
        return ResponseEntity.ok(dictionaryService.getAllInspections());
    }

    @PostMapping("/inspections")
    public ResponseEntity<Inspection> createInspection(@RequestBody Inspection inspection, Authentication auth) {
        Inspection saved = dictionaryService.createInspection(inspection);
        logDict("CREATE", "тип осмотра", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/inspections/{id}")
    public ResponseEntity<Inspection> updateInspection(@PathVariable Long id, @RequestBody Inspection inspection, Authentication auth) {
        Inspection updated = dictionaryService.updateInspection(id, inspection);
        logDict("UPDATE", "тип осмотра", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/inspections/{id}")
    public ResponseEntity<Void> deleteInspection(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "тип осмотра", id, null, auth);
        dictionaryService.deleteInspection(id);
        return ResponseEntity.noContent().build();
    }

    //Transfer

    @GetMapping("/transfers")
    public ResponseEntity<List<Transfer>> getTransfers() {
        return ResponseEntity.ok(dictionaryService.getAllTransfers());
    }

    @PostMapping("/transfers")
    public ResponseEntity<Transfer> createTransfer(@RequestBody Transfer transfer, Authentication auth) {
        Transfer saved = dictionaryService.createTransfer(transfer);
        logDict("CREATE", "путь передачи", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/transfers/{id}")
    public ResponseEntity<Transfer> updateTransfer(@PathVariable Long id, @RequestBody Transfer transfer, Authentication auth) {
        Transfer updated = dictionaryService.updateTransfer(id, transfer);
        logDict("UPDATE", "путь передачи", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/transfers/{id}")
    public ResponseEntity<Void> deleteTransfer(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "путь передачи", id, null, auth);
        dictionaryService.deleteTransfer(id);
        return ResponseEntity.noContent().build();
    }

    //CitizenCategory

    @GetMapping("/citizen-categories")
    public ResponseEntity<List<CitizenCategory>> getCitizenCategories() {
        return ResponseEntity.ok(dictionaryService.getAllCitizenCategories());
    }

    @PostMapping("/citizen-categories")
    public ResponseEntity<CitizenCategory> createCitizenCategory(@RequestBody CitizenCategory citizenCategory, Authentication auth) {
        CitizenCategory saved = dictionaryService.createCitizenCategory(citizenCategory);
        logDict("CREATE", "категория проживания", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/citizen-categories/{id}")
    public ResponseEntity<CitizenCategory> updateCitizenCategory(@PathVariable Long id, @RequestBody CitizenCategory citizenCategory, Authentication auth) {
        CitizenCategory updated = dictionaryService.updateCitizenCategory(id, citizenCategory);
        logDict("UPDATE", "категория проживания", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/citizen-categories/{id}")
    public ResponseEntity<Void> deleteCitizenCategory(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "категория проживания", id, null, auth);
        dictionaryService.deleteCitizenCategory(id);
        return ResponseEntity.noContent().build();
    }

    //CitizenType

    @GetMapping("/citizen-types")
    public ResponseEntity<List<CitizenType>> getCitizenTypes() {
        return ResponseEntity.ok(dictionaryService.getAllCitizenTypes());
    }

    @PostMapping("/citizen-types")
    public ResponseEntity<CitizenType> createCitizenType(@RequestBody CitizenType citizenType, Authentication auth) {
        CitizenType saved = dictionaryService.createCitizenType(citizenType);
        logDict("CREATE", "тип нас. пункта", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/citizen-types/{id}")
    public ResponseEntity<CitizenType> updateCitizenType(@PathVariable Long id, @RequestBody CitizenType citizenType, Authentication auth) {
        CitizenType updated = dictionaryService.updateCitizenType(id, citizenType);
        logDict("UPDATE", "тип нас. пункта", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/citizen-types/{id}")
    public ResponseEntity<Void> deleteCitizenType(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "тип нас. пункта", id, null, auth);
        dictionaryService.deleteCitizenType(id);
        return ResponseEntity.noContent().build();
    }

    //SocialGroup

    @GetMapping("/social-groups")
    public ResponseEntity<List<SocialGroup>> getSocialGroups() {
        return ResponseEntity.ok(dictionaryService.getAllSocialGroups());
    }

    @PostMapping("/social-groups")
    public ResponseEntity<SocialGroup> createSocialGroup(@RequestBody SocialGroup socialGroup, Authentication auth) {
        SocialGroup saved = dictionaryService.createSocialGroup(socialGroup);
        logDict("CREATE", "соц. группа", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/social-groups/{id}")
    public ResponseEntity<SocialGroup> updateSocialGroup(@PathVariable Long id, @RequestBody SocialGroup socialGroup, Authentication auth) {
        SocialGroup updated = dictionaryService.updateSocialGroup(id, socialGroup);
        logDict("UPDATE", "соц. группа", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/social-groups/{id}")
    public ResponseEntity<Void> deleteSocialGroup(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "соц. группа", id, null, auth);
        dictionaryService.deleteSocialGroup(id);
        return ResponseEntity.noContent().build();
    }

    //LaboratoryTestType

    @GetMapping("/lab-test-types")
    public ResponseEntity<List<LaboratoryTestType>> getLaboratoryTestTypes() {
        return ResponseEntity.ok(dictionaryService.getAllLaboratoryTestTypes());
    }

    @PostMapping("/lab-test-types")
    public ResponseEntity<LaboratoryTestType> createLaboratoryTestType(@RequestBody LaboratoryTestType laboratoryTestType, Authentication auth) {
        LaboratoryTestType saved = dictionaryService.createLaboratoryTestType(laboratoryTestType);
        logDict("CREATE", "лаб. тест", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/lab-test-types/{id}")
    public ResponseEntity<LaboratoryTestType> updateLaboratoryTestType(@PathVariable Long id, @RequestBody LaboratoryTestType laboratoryTestType, Authentication auth) {
        LaboratoryTestType updated = dictionaryService.updateLaboratoryTestType(id, laboratoryTestType);
        logDict("UPDATE", "лаб. тест", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/lab-test-types/{id}")
    public ResponseEntity<Void> deleteLaboratoryTestType(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "лаб. тест", id, null, auth);
        dictionaryService.deleteLaboratoryTestType(id);
        return ResponseEntity.noContent().build();
    }

    // Branch

    @GetMapping("/branches")
    public ResponseEntity<List<Branch>> getBranches() {
        return ResponseEntity.ok(dictionaryService.getAllBranches());
    }

    @PostMapping("/branches")
    public ResponseEntity<Branch> createBranch(@RequestBody Branch branch, Authentication auth) {
        Branch saved = dictionaryService.createBranch(branch);
        logDict("CREATE", "филиал", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/branches/{id}")
    public ResponseEntity<Branch> updateBranch(@PathVariable Long id, @RequestBody Branch branch, Authentication auth) {
        Branch updated = dictionaryService.updateBranch(id, branch);
        logDict("UPDATE", "филиал", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/branches/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "филиал", id, null, auth);
        dictionaryService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }

    //Department

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(dictionaryService.getAllDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department, Authentication auth) {
        Department saved = dictionaryService.createDepartment(department);
        logDict("CREATE", "отделение", saved.getId(), saved.getName(), auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department, Authentication auth) {
        Department updated = dictionaryService.updateDepartment(id, department);
        logDict("UPDATE", "отделение", id, updated.getName(), auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "отделение", id, null, auth);
        dictionaryService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    //Population

    @GetMapping("/population")
    public ResponseEntity<List<Population>> getPopulations() {
        return ResponseEntity.ok(dictionaryService.getAllPopulations());
    }

    @PostMapping("/population")
    public ResponseEntity<Population> createPopulation(@RequestBody Population population, Authentication auth) {
        Population saved = dictionaryService.createPopulation(population);
        logDict("CREATE", "население", saved.getId(), null, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/population/{id}")
    public ResponseEntity<Population> updatePopulation(@PathVariable Long id, @RequestBody Population population, Authentication auth) {
        Population updated = dictionaryService.updatePopulation(id, population);
        logDict("UPDATE", "население", id, null, auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/population/{id}")
    public ResponseEntity<Void> deletePopulation(@PathVariable Long id, Authentication auth) {
        logDict("DELETE", "население", id, null, auth);
        dictionaryService.deletePopulation(id);
        return ResponseEntity.noContent().build();
    }
}
