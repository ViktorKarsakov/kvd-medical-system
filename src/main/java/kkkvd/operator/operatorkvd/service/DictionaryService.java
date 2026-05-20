package kkkvd.operator.operatorkvd.service;

import kkkvd.operator.operatorkvd.entities.*;
import kkkvd.operator.operatorkvd.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final DetectionCaseRepository detectionCaseRepository;
    private final BranchRepository branchRepository;
    private final CitizenCategoryRepository citizenCategoryRepository;
    private final CitizenTypeRepository citizenTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final GenderRepository genderRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisGroupRepository diagnosisGroupRepository;
    private final DoctorRepository doctorRepository;
    private final StateRepository stateRepository;
    private final StateGroupRepository stateGroupRepository;
    private final PlaceRepository placeRepository;
    private final ProfileRepository profileRepository;
    private final InspectionRepository inspectionRepository;
    private final TransferRepository transferRepository;
    private final SocialGroupRepository socialGroupRepository;
    private final LaboratoryTestTypeRepository laboratoryTestTypeRepository;
    private final PatientRepository patientRepository;
    private final DetectionCaseLabTestRepository detectionCaseLabTestRepository;
    private final PopulationRepository populationRepository;

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    public List<CitizenCategory> getAllCitizenCategories() {
        return citizenCategoryRepository.findAll();
    }

    public List<CitizenType> getAllCitizenTypes() {
        return citizenTypeRepository.findAll();
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Gender> getAllGenders() {
        return genderRepository.findAll();
    }

    public List<Diagnosis> getAllDiagnoses() {
        return diagnosisRepository.findAllByOrderByNameAsc();
    }

    public List<DiagnosisGroup> getAllDiagnosisGroups() {
        return diagnosisGroupRepository.findAll();
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }

    public List<State> getAllStates() {
        return stateRepository.findAllByOrderByStateGroupIdAscNameAsc();
    }

    public List<StateGroup> getAllStateGroups() {
        return stateGroupRepository.findAll();
    }

    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public List<Inspection> getAllInspections() {
        return inspectionRepository.findAll();
    }

    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    public List<SocialGroup> getAllSocialGroups() {
        return socialGroupRepository.findAll();
    }

    public List<LaboratoryTestType> getAllLaboratoryTestTypes() {
        return laboratoryTestTypeRepository.findAll();
    }

    public List<Population> getAllPopulations() {
        return populationRepository.findAll();
    }

    //created

    @Transactional
    public Gender createGender(Gender gender) {
        return genderRepository.save(gender);
    }

    @Transactional
    public Diagnosis createDiagnosis(Diagnosis diagnosis) {
        return diagnosisRepository.save(diagnosis);
    }

    @Transactional
    public DiagnosisGroup createDiagnosisGroup(DiagnosisGroup diagnosisGroup) {
        return diagnosisGroupRepository.save(diagnosisGroup);
    }

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional
    public State createState(State state) {
        return stateRepository.save(state);
    }

    @Transactional
    public StateGroup createStateGroup(StateGroup stateGroup) {
        return stateGroupRepository.save(stateGroup);
    }

    @Transactional
    public Place createPlace(Place place) {
        return placeRepository.save(place);
    }

    @Transactional
    public Profile createProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    @Transactional
    public Inspection createInspection(Inspection inspection) {
        return inspectionRepository.save(inspection);
    }

    @Transactional
    public Transfer createTransfer(Transfer transfer) {
        return transferRepository.save(transfer);
    }

    @Transactional
    public CitizenCategory createCitizenCategory(CitizenCategory citizenCategory) {
        return citizenCategoryRepository.save(citizenCategory);
    }

    @Transactional
    public CitizenType createCitizenType(CitizenType citizenType) {
        return citizenTypeRepository.save(citizenType);
    }

    @Transactional
    public SocialGroup createSocialGroup(SocialGroup socialGroup) {
        return socialGroupRepository.save(socialGroup);
    }

    @Transactional
    public LaboratoryTestType createLaboratoryTestType(LaboratoryTestType labTestType) {
        return laboratoryTestTypeRepository.save(labTestType);
    }

    @Transactional
    public Branch createBranch(Branch branch) {
        return branchRepository.save(branch);
    }

    @Transactional
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Transactional
    public Population createPopulation(Population population) {
        return populationRepository.save(population);
    }

    //update
    @Transactional
    public Gender updateGender(Long id, Gender gender) {
        Gender existing = genderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пол не найден"));
        existing.setName(gender.getName());
        return genderRepository.save(existing);
    }

    @Transactional
    public DiagnosisGroup updateDiagnosisGroup(Long id, DiagnosisGroup diagnosisGroup) {
        DiagnosisGroup existing = diagnosisGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа диагнозов не найдена"));
        existing.setName(diagnosisGroup.getName());
        return diagnosisGroupRepository.save(existing);
    }

    @Transactional
    public Diagnosis updateDiagnosis(Long id, Diagnosis diagnosis) {
        Diagnosis existing = diagnosisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Диагноз не найден"));
        existing.setName(diagnosis.getName());
        existing.setDiagnosisGroup(diagnosis.getDiagnosisGroup());
        return diagnosisRepository.save(existing);
    }

    @Transactional
    public Branch updateBranch(Long id, Branch branch) {
        Branch existing = branchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Филиал не найден"));
        existing.setName(branch.getName());
        return branchRepository.save(existing);
    }

    @Transactional
    public Department updateDepartment(Long id, Department department) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Отделение не найдено"));
        existing.setName(department.getName());
        existing.setBranch(department.getBranch());
        return departmentRepository.save(existing);
    }

    @Transactional
    public Doctor updateDoctor(Long id, Doctor doctor) {
        Doctor existing = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Врач не найден"));
        existing.setLastName(doctor.getLastName());
        existing.setFirstName(doctor.getFirstName());
        existing.setMiddleName(doctor.getMiddleName());
        existing.setDepartment(doctor.getDepartment());
        return doctorRepository.save(existing);
    }

    @Transactional
    public StateGroup updateStateGroup(Long id, StateGroup stateGroup) {
        StateGroup existing = stateGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа районов не найдена"));
        existing.setName(stateGroup.getName());
        return stateGroupRepository.save(existing);
    }

    @Transactional
    public State updateState(Long id, State state) {
        State existing = stateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Район не найден"));
        existing.setName(state.getName());
        existing.setStateGroup(state.getStateGroup());
        return stateRepository.save(existing);
    }

    @Transactional
    public Place updatePlace(Long id, Place place) {
        Place existing = placeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Место выявления не найдено"));
        existing.setName(place.getName());
        return placeRepository.save(existing);
    }

    @Transactional
    public Profile updateProfile(Long id, Profile profile) {
        Profile existing = profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Профиль не найден"));
        existing.setName(profile.getName());
        return profileRepository.save(existing);
    }

    @Transactional
    public Inspection updateInspection(Long id, Inspection inspection) {
        Inspection existing = inspectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тип осмотра не найден"));
        existing.setName(inspection.getName());
        return inspectionRepository.save(existing);
    }

    @Transactional
    public Transfer updateTransfer(Long id, Transfer transfer) {
        Transfer existing = transferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Путь передачи не найден"));
        existing.setName(transfer.getName());
        return transferRepository.save(existing);
    }

    @Transactional
    public CitizenCategory updateCitizenCategory(Long id, CitizenCategory citizenCategory) {
        CitizenCategory existing = citizenCategoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Категория проживания не найдена"));
        existing.setName(citizenCategory.getName());
        return citizenCategoryRepository.save(existing);
    }

    @Transactional
    public CitizenType updateCitizenType(Long id, CitizenType citizenType) {
        CitizenType existing = citizenTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тип населённого пункта не найден"));
        existing.setName(citizenType.getName());
        return citizenTypeRepository.save(existing);
    }

    @Transactional
    public SocialGroup updateSocialGroup(Long id, SocialGroup socialGroup) {
        SocialGroup existing = socialGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Социальная группа не найдена"));
        existing.setName(socialGroup.getName());
        return socialGroupRepository.save(existing);
    }

    @Transactional
    public LaboratoryTestType updateLaboratoryTestType(Long id, LaboratoryTestType labTestType) {
        LaboratoryTestType existing = laboratoryTestTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тип лаб. теста не найден"));
        existing.setName(labTestType.getName());
        return laboratoryTestTypeRepository.save(existing);
    }

    @Transactional
    public Population updatePopulation(Long id, Population population) {
        Population existing = populationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена"));
        existing.setState(population.getState());
        existing.setYear(population.getYear());
        existing.setCountAll(population.getCountAll());
        return populationRepository.save(existing);
    }

    //delete
    @Transactional
    public void deleteGender(Long id) {
        if (!genderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пол не найден");
        }
        if (patientRepository.countByGenderId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: пол используется в записях пациентов");
        }
        genderRepository.deleteById(id);
    }

    @Transactional
    public void deleteDiagnosisGroup(Long id) {
        if (!diagnosisGroupRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа диагнозов не найдена");
        }
        if (diagnosisRepository.countByDiagnosisGroupId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: в группе есть диагнозы");
        }
        diagnosisGroupRepository.deleteById(id);
    }

    @Transactional
    public void deleteDiagnosis(Long id) {
        if (!diagnosisRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Диагноз не найден");
        }
        if (detectionCaseRepository.countByDiagnosisId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: диагноз используется в случаях заболевания");
        }
        diagnosisRepository.deleteById(id);
    }

    @Transactional
    public void deleteBranch(Long id) {
        if (!branchRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Филиал не найден");
        }
        if (departmentRepository.countByBranchId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: в филиале есть отделения");
        }
        branchRepository.deleteById(id);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Отделение не найдено");
        }
        if (doctorRepository.countByDepartmentId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: в отделении есть врачи");
        }
        departmentRepository.deleteById(id);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Врач не найден");
        }
        if (detectionCaseRepository.countByDoctorId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: врач привязан к случаям заболевания");
        }
        doctorRepository.deleteById(id);
    }

    @Transactional
    public void deleteStateGroup(Long id) {
        if (!stateGroupRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа районов не найдена");
        }
        if (stateRepository.countByStateGroupId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: в группе есть районы");
        }
        stateGroupRepository.deleteById(id);
    }

    @Transactional
    public void deleteState(Long id) {
        if (!stateRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Район не найден");
        }
        if (detectionCaseRepository.countByStateId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: район используется в случаях заболевания");
        }
        stateRepository.deleteById(id);
    }

    @Transactional
    public void deletePlace(Long id) {
        if (!placeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Место выявления не найдено");
        }
        if (detectionCaseRepository.countByPlaceId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: место выявления используется в случаях заболевания");
        }
        placeRepository.deleteById(id);
    }

    @Transactional
    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Профиль не найден");
        }
        if (detectionCaseRepository.countByProfileId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: профиль используется в случаях заболевания");
        }
        profileRepository.deleteById(id);
    }

    @Transactional
    public void deleteInspection(Long id) {
        if (!inspectionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Тип осмотра не найден");
        }
        if (detectionCaseRepository.countByInspectionId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: тип осмотра используется в случаях заболевания");
        }
        inspectionRepository.deleteById(id);
    }

    @Transactional
    public void deleteTransfer(Long id) {
        if (!transferRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Путь передачи не найден");
        }
        if (detectionCaseRepository.countByTransferId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: путь передачи используется в случаях заболевания");
        }
        transferRepository.deleteById(id);
    }

    @Transactional
    public void deleteCitizenCategory(Long id) {
        if (!citizenCategoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Категория проживания не найдена");
        }
        if (detectionCaseRepository.countByCitizenCategoryId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: категория проживания используется в случаях заболевания");
        }
        citizenCategoryRepository.deleteById(id);
    }

    @Transactional
    public void deleteCitizenType(Long id) {
        if (!citizenTypeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Тип населённого пункта не найден");
        }
        if (detectionCaseRepository.countByCitizenTypeId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: тип населённого пункта используется в случаях заболевания");
        }
        citizenTypeRepository.deleteById(id);
    }

    @Transactional
    public void deleteSocialGroup(Long id) {
        if (!socialGroupRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Социальная группа не найдена");
        }
        if (detectionCaseRepository.countBySocialGroupId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: социальная группа используется в случаях заболевания");
        }
        socialGroupRepository.deleteById(id);
    }

    @Transactional
    public void deleteLaboratoryTestType(Long id) {
        if (!laboratoryTestTypeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Тип лаб. теста не найден");
        }
        if (detectionCaseLabTestRepository.countByLaboratoryTestTypeId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить: тип лаб. теста используется в случаях заболевания");
        }
        laboratoryTestTypeRepository.deleteById(id);
    }

    @Transactional
    public void deletePopulation(Long id) {
        if (!populationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена");
        }
        populationRepository.deleteById(id);
    }
}
