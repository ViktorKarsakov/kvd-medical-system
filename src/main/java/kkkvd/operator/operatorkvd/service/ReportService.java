package kkkvd.operator.operatorkvd.service;

import kkkvd.operator.operatorkvd.dto.ReportRequest;
import kkkvd.operator.operatorkvd.entities.*;
import kkkvd.operator.operatorkvd.repositories.*;
import kkkvd.operator.operatorkvd.util.DoctorNameFormatter;
import kkkvd.operator.operatorkvd.util.NameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final DetectionCaseRepository detectionCaseRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisGroupRepository diagnosisGroupRepository;
    private final StateRepository stateRepository;
    private final PopulationRepository populationRepository;

    private static final String GENDER_MALE = "MALE";
    private static final String GENDER_FEMALE = "FEMALE";
    private static final String CITIZEN_TYPE_RURAL = "RURAL";
    //Группа районов города Красноярска
    private static final String STATE_GROUP_CITY = "KRASNOYARSK";
    private static final Set<String> DDU_CODES = Set.of("KINDERGARTEN_ORG", "KINDERGARTEN_UNORG");
    private static final String[] INDICATORS_GROUP_CODES = {
            "SYPHILIS", "GONORRHEA", "SCABIES", "TRICHOMONIASIS",
            "CHLAMYDIA", "HERPES", "CONDYLOMAS", "MYCOSES", "MICROSPORIA"
    };
    private static final String SYPHILIS_CODE = "SYPHILIS";
    private static final String[] PER100K_GROUP_CODES = {
            "SYPHILIS", "GONORRHEA", "SCABIES", "MICROSPORIA", "MYCOSES"
    };
    private static final Set<String> GROUPS_WITH_AGE_SUBSECTIONS = Set.of(
            "MYCOSES", "MICROSPORIA"
    );
    private static final Set<String> GROUPS_WITH_PROFILE_SECTION = Set.of(
            "SYPHILIS", "CONDYLOMAS", "MICROSPORIA", "TRICHOMONIASIS"
    );

    private List<Long> resolveStateIds(ReportRequest request) {
        switch (request.getRegionFilter()) {
            case "CITY":
                return stateRepository.findByStateGroupCode(STATE_GROUP_CITY)
                        .stream().map(State::getId).collect(Collectors.toList());
            case "STATE":
                return List.of((request.getStateId()));
            default:
                return stateRepository.findAll()
                        .stream().map(State::getId).collect(Collectors.toList());
        }
    }
    //Расчет возраста в полных годах
    private int calculateAge(LocalDate birthDate, LocalDate diagnosisDate) {
        return (int) ChronoUnit.YEARS.between(birthDate, diagnosisDate);
    }

    //Загрузить все случаи за период по районам
    private List<DetectionCase> fetchCases(LocalDate from, LocalDate to, List<Long> stateIds) {
        return detectionCaseRepository.findAllForReports(from, to, stateIds);
    }

    //Проверка на мужской пол по code
    private boolean isMale(DetectionCase dc) {
        return GENDER_MALE.equals(dc.getPatient().getGender().getCode());
    }

    //Проверка из села по code
    private boolean isRural(DetectionCase dc) {
        return CITIZEN_TYPE_RURAL.equals(dc.getCitizenType().getCode());
    }

    //Проверка ДДУ по двум кодам
    private boolean isDdu(DetectionCase dc) {
        return DDU_CODES.contains(dc.getSocialGroup().getCode());
    }

    //Относится ли район к Красноярску
    private boolean isCityDistrict(DetectionCase dc) {
        return STATE_GROUP_CITY.equals(dc.getState().getStateGroup().getCode());
    }

    //Фильтр случаев по ид группы диагнозов
    private List<DetectionCase> filterByDiagnosisGroup(List<DetectionCase> dc, Long groupId) {
        return dc.stream()
                .filter(c -> c.getDiagnosis().getDiagnosisGroup().getId().equals(groupId))
                .collect(Collectors.toList());
    }

    //Фильтр случаев по возрасту
    private List<DetectionCase> filterByAge(List<DetectionCase> dc, int minAge, int maxAge) {
        return dc.stream()
                .filter(c -> {
                    int age = calculateAge(c.getPatient().getBirthDate(), c.getDiagnosisDate());
                    return age >= minAge && age <= maxAge;
                })
                .collect(Collectors.toList());
    }

    //Имя группы диагнозов для заголовков excel
    public String getDiagnosisGroupName(Long groupId) {
        return diagnosisGroupRepository.findById(groupId)
                .map(DiagnosisGroup::getName).orElse("Неизвестно");
    }

    //Достаем из БД по id группы диагнозов code, чтобы понять какая это группа по коду
    private String getDiagnosisGroupCode(Long groupId) {
        return diagnosisGroupRepository.findById(groupId)
                .map(DiagnosisGroup::getCode).orElse("");
    }

    //Метод для контроллера, чтобы понять, сифилис ли это
    public boolean isSyphilisGroup(Long groupId) {
        return SYPHILIS_CODE.equals(getDiagnosisGroupCode(groupId));
    }



    //Отображаемое имя для заголовков столбцов
    private Map<String, String> buildGroupCodeToNameMap() {
        return diagnosisGroupRepository.findAll().stream()
                .collect(Collectors.toMap(DiagnosisGroup::getCode, DiagnosisGroup::getName));
    }

    //Строка ИТОГО для суммирования числовых столбцов
    private Map<String, Object> buildTotalRow(List<Map<String, Object>> rows, String labelKey, String[] sumKeys) {
        Map<String, Object> total = new LinkedHashMap<>();
        total.put(labelKey, "ИТОГО");
        for (String key : sumKeys) {
            int sum = rows.stream()
                    .map(r -> r.get(key))
                    .filter(Objects::nonNull)
                    .mapToInt(v -> ((Number) v).intValue())
                    .sum();
            total.put(key, sum > 0 ? sum : null);
        }
        return total;
    }

    //Один метод для 4 отчётов (Сифилис / Гонорея / Микроспория / Чесотка)
    //Строки = районы
    //Столбцы: Район | Все | До 17 | До 14 | До 1 | 1-2 | 3-6 | 3-6 ДДУ | Село | До 17 (село)

    public List<Map<String, Object>> generateStructureReport(ReportRequest request) {
        List<Long> stateIds = resolveStateIds(request);
        List<DetectionCase> allCases = fetchCases(request.getDateFrom(), request.getDateTo(), stateIds);
        List<DetectionCase> cases = filterByDiagnosisGroup(allCases, request.getDiagnosisGroupId());

        Map<String, List<DetectionCase>> byDistrict = cases.stream()
                .collect(Collectors.groupingBy(c -> c.getState().getName(), LinkedHashMap::new, Collectors.toList()));
        List<Map<String, Object>> rows = new ArrayList<>();

        for(var entry : byDistrict.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("district", entry.getKey());
            List<DetectionCase> dc = entry.getValue();
            row.put("total", dc.size());

            int under17 = 0, under14 = 0, under1 = 0;
            int age1to2 = 0, age3to6 = 0, age3to6ddu = 0;
            int rural = 0, under17rural = 0;

            for (DetectionCase c : dc) {
                int age = calculateAge(c.getPatient().getBirthDate(), c.getDiagnosisDate());
                boolean r = isRural(c);

                if (age <= 17) under17++;
                if (age <= 14) under14++;
                if (age < 1) under1++;
                if (age >= 1 && age <= 2) age1to2++;
                if (age >= 3 && age <= 6) age3to6++;
                if (age >= 3 && age <= 6 && isDdu(c)) age3to6ddu++;
                if (r) rural++;
                if (age <= 17 && r) under17rural++;
            }

            row.put("under17", under17 > 0 ? under17 : null);
            row.put("under14", under14 > 0 ? under14 : null);
            row.put("under1",  under1 > 0 ? under1 : null);
            row.put("age1to2",  age1to2 > 0 ? age1to2 : null);
            row.put("age3to6",  age3to6 > 0 ? age3to6 : null);
            row.put("age3to6ddu",  age3to6ddu > 0 ? age3to6ddu : null);
            row.put("rural",  rural > 0 ? rural : null);
            row.put("under17rural",  under17rural > 0 ? under17rural : null);
            rows.add(row);
        }

        String[] sumKeys = {"total", "under17", "under14", "under1", "age1to2", "age3to6", "age3to6ddu", "rural", "under17rural"};
        rows.add(buildTotalRow(rows, "district", sumKeys));
        return rows;
    }

    //Показатели заболеваемости. Строки = районы, столбцы = заболевания
    public List<Map<String, Object>> generateIndicatorsReport(ReportRequest request) {
        List<Long> stateIds = resolveStateIds(request);
        List<DetectionCase> cases = fetchCases(request.getDateFrom(), request.getDateTo(), stateIds);
        Map<String, String> codeToName = buildGroupCodeToNameMap();

        Map<String, List<DetectionCase>> byDistrict = cases.stream()
                .collect(Collectors.groupingBy(c -> c.getState().getName(), LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> rows = new ArrayList<>();

        for (var entry : byDistrict.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("district", entry.getKey());

            for (String groupCode : INDICATORS_GROUP_CODES) {
                long count = entry.getValue().stream()
                        .filter(c -> c.getDiagnosis().getDiagnosisGroup().getCode().equals(groupCode)).count();
                String name = codeToName.getOrDefault(groupCode, groupCode);
                row.put(name, count > 0 ? count : null);
            }
            rows.add(row);
        }
        return rows;
    }

    //Заболеваемость ИППП на 100 тыс. населения
    public List<Map<String, Object>> generateIpppPer100kReport(ReportRequest request) {
        List<Long> stateIds = resolveStateIds(request);
        List<DetectionCase> cases = fetchCases(request.getDateFrom(), request.getDateTo(), stateIds);
        Map<String, String> codeToName = buildGroupCodeToNameMap();

        int year = request.getDateFrom().getYear();

        Map<Long, Integer> popByState = new HashMap<>();
        for (Population p : populationRepository.findByYear(year)) {
            popByState.put(p.getState().getId(), p.getCountAll());
        }

        Map<String, List<DetectionCase>> byDistrict = cases.stream()
                .collect(Collectors.groupingBy(c -> c.getState().getName(), LinkedHashMap::new, Collectors.toList()));
        List<Map<String, Object>> rows = new ArrayList<>();

        for (var entry : byDistrict.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("district", entry.getKey());
            Long stId = entry.getValue().get(0).getState().getId();
            Integer pop = popByState.get(stId);

            for (String groupCode : PER100K_GROUP_CODES) {
                String name = codeToName.getOrDefault(groupCode, groupCode);
                List<DetectionCase> dCases = entry.getValue().stream()
                        .filter(c -> c.getDiagnosis().getDiagnosisGroup().getCode().equals(groupCode)).toList();
                int count = dCases.size();

                row.put(name + " Всего", count > 0 ? count : null);
                row.put(name + " на 100т.", count > 0 && pop != null && pop > 0 ? Math.round((double) count * 100000 / pop * 10.0) / 10.0 : null);

                long teens = dCases.stream().filter(c -> {
                    int age = calculateAge(c.getPatient().getBirthDate(), c.getDiagnosisDate());
                    return age >= 15 && age <= 17;
                }).count();
                row.put(name + " 15-17 лет", teens > 0 ? (int) teens : null);
            }
            rows.add(row);
        }

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("district", "ИТОГО");
        for (String groupCode : PER100K_GROUP_CODES) {
            String name = codeToName.getOrDefault(groupCode, groupCode);
            for (String s : new String[]{" Всего", " на 100т.", " 15-17 лет"}) {
                String key = name + s;
                if (s.equals(" на 100т.")) {
                    total.put(key, null);
                } else {
                    int sum = rows.stream().map(r -> r.get(key))
                            .filter(Objects::nonNull)
                            .mapToInt(v -> ((Number) v).intValue()).sum();
                    total.put(key, sum > 0 ? sum : null);
                }
            }
        }
        rows.add(total);
        return rows;
    }

    //Сведения о заболеваниях ИППП. Строки = конкретный диагноз + пол
    //Столбцы: Диагноз | Пол | Кол-во | возрастные группы | сельские
    public List<Map<String, Object>> generateIpppDetailsReport(ReportRequest request) {
        List<Long> stateIds = resolveStateIds(request);
        List<DetectionCase> cases = fetchCases(request.getDateFrom(), request.getDateTo(), stateIds);

        Map<String, Map<String, List<DetectionCase>>> grouped = new TreeMap<>();
        for (DetectionCase dc : cases) {
            grouped.computeIfAbsent(dc.getDiagnosis().getName(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(dc.getPatient().getGender().getName(), k -> new ArrayList<>())
                    .add(dc);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (var diagEntry : grouped.entrySet()) {
            for (var genderEntry : diagEntry.getValue().entrySet()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("diagnosis", diagEntry.getKey());
                row.put("gender", genderEntry.getKey());
                List<DetectionCase> list = genderEntry.getValue();
                row.put("count", list.size());

                int a01 = 0, a214 = 0, a1517 = 0, a1829 = 0, a3039 = 0, a40 = 0;
                int rur = 0, r01 = 0, r214 = 0, r1517 = 0;

                for (DetectionCase dc : list) {
                    int age = calculateAge(dc.getPatient().getBirthDate(), dc.getDiagnosisDate());
                    boolean r = isRural(dc);
                    if (age <= 1) a01++;
                    else if (age <= 14) a214++;
                    else if (age <= 17) a1517++;
                    else if (age <= 29) a1829++;
                    else if (age <= 39) a3039++;
                    else a40++;
                    if (r) {
                        rur++;
                        if (age <= 1) r01++;
                        else if (age <= 14) r214++;
                        else if (age <= 17) r1517++;
                    }
                }

                row.put("age0_1", a01 > 0 ? a01 : null);
                row.put("age2_14", a214 > 0 ? a214 : null);
                row.put("age15_17", a1517 > 0 ? a1517 : null);
                row.put("age18_29", a1829 > 0 ? a1829 : null);
                row.put("age30_39", a3039 > 0 ? a3039 : null);
                row.put("age40plus", a40 > 0 ? a40 : null);
                row.put("rural", rur > 0 ? rur : null);
                row.put("rural0_1", r01 > 0 ? r01 : null);
                row.put("rural2_14", r214 > 0 ? r214 : null);
                row.put("rural15_17", r1517 > 0 ? r1517 : null);
                rows.add(row);
            }
        }
        return rows;
    }

    //Список больных по врачам и району
    //Плоский список: Пациент | Дата рождения | Диагноз | Район | Врач
    public List<Map<String, Object>> generateDoctorPatientsReport(ReportRequest request) {
        List<Long> stateIds = resolveStateIds(request);
        List<DetectionCase> cases = fetchCases(request.getDateFrom(), request.getDateTo(), stateIds);

        cases.sort(Comparator.comparing((DetectionCase c) -> c.getDiagnosis().getName())
                .thenComparing(c -> c.getDoctor().getLastName())
                .thenComparing(c -> c.getState().getName())
                .thenComparing(c -> c.getPatient().getLastName()));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (DetectionCase dc : cases) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("patient", NameUtils.buildFullName(dc.getPatient()));
            row.put("birthDate", dc.getPatient().getBirthDate());
            row.put("diagnosis", dc.getDiagnosis().getName());
            row.put("district", dc.getState().getName());
            row.put("doctor", DoctorNameFormatter.formatShort(dc.getDoctor()));
            rows.add(row);
        }
        return rows;
    }

    //Подробный отчёт по заболеванию
    //Состоит из секций, каждая — отдельная таблица в Excel. Возвращает Map: ключ = название секции, значение = строки.
    public Map<String, List<Map<String, Object>>> generateDetailedReport(ReportRequest request) {
        List<Long> stateIds = resolveStateIds(request);
        List<DetectionCase> allCases = fetchCases(request.getDateFrom(), request.getDateTo(), stateIds);
        List<DetectionCase> cases = filterByDiagnosisGroup(allCases, request.getDiagnosisGroupId());

        String groupCode = getDiagnosisGroupCode(request.getDiagnosisGroupId());
        boolean isSyph = SYPHILIS_CODE.equals(groupCode);
        boolean hasAgeSubs = GROUPS_WITH_AGE_SUBSECTIONS.contains(groupCode);
        boolean hasProfile = GROUPS_WITH_PROFILE_SECTION.contains(groupCode);
        int totalCount = cases.size();
        int year = request.getDateFrom().getYear();
        Long gId = request.getDiagnosisGroupId();

        Map<String, List<Map<String, Object>>> sections = new LinkedHashMap<>();
        //профиль врача (не у всех заболеваний)
        if (hasProfile) {
            sections.put("Профиль врача (Поликлиника)", buildGenderSection(cases, c -> c.getProfile().getName(), totalCount, isSyph, gId));
        }

        //Секции с опциональными подсекциями для детей/подростков
        addSectionWithAgeSubs(sections, "Место выявления", cases, c -> c.getPlace().getName(), totalCount, isSyph, hasAgeSubs, gId);
        addSectionWithAgeSubs(sections, "Социальная группа", cases, c -> c.getSocialGroup().getName(), totalCount, isSyph, hasAgeSubs, gId);
        addSectionWithAgeSubs(sections, "Категория жителя", cases, c -> c.getCitizenCategory().getName(), totalCount, isSyph, hasAgeSubs, gId);
        addSectionWithAgeSubs(sections, "Тип населенного пункта", cases, c -> c.getCitizenType().getName(), totalCount, isSyph, hasAgeSubs, gId);
        addSectionWithAgeSubs(sections, "Тип осмотра", cases, c -> c.getInspection().getName(), totalCount, isSyph, hasAgeSubs, gId);

        //Район проживания
        sections.put("Район проживания", buildDistrictSection(cases, isSyph, year, gId));
        if (hasAgeSubs) {
            var teens = filterByAge(cases, 15, 17);
            var kids = filterByAge(cases, 0, 14);
            sections.put("Район проживания Подростки 15-17 лет", buildDistrictSection(teens, false, year, gId));
            sections.put("Район проживания Дети 0-14 лет", buildDistrictSection(kids, false, year, gId));
        }
        sections.put("Возрастная группа", buildAgeGroupSection(cases, isSyph, gId));
        return sections;
    }

    //Строители секций
    //Добавляет секцию + подсекции для подростков/детей (если нужно)
    private void addSectionWithAgeSubs(
            Map<String, List<Map<String, Object>>> sections,
            String name, List<DetectionCase> cases,
            Function<DetectionCase, String> grouper,
            int total, boolean isSyph, boolean hasSubs, Long gId) {

        sections.put(name,
                buildGenderSection(cases, grouper, total, isSyph, gId));
        if (hasSubs) {
            var teens = filterByAge(cases, 15, 17);
            var kids = filterByAge(cases, 0, 14);
            sections.put(name + " Подростки 15-17 лет",
                    buildGenderSection(teens, grouper, teens.size(), false, gId));
            sections.put(name + " Дети 0-14 лет",
                    buildGenderSection(kids, grouper, kids.size(), false, gId));
        }
    }

    /**
     * Секция: стандартный формат (Муж/Жен/Всего/%)
     * или формат Сифилиса (столбцы = МКБ-коды).
     */
    private List<Map<String, Object>> buildGenderSection(
            List<DetectionCase> cases,
            Function<DetectionCase, String> grouper,
            int totalForPercent, boolean isSyph, Long diagGroupId) {

        Map<String, List<DetectionCase>> grouped = cases.stream()
                .collect(Collectors.groupingBy(
                        grouper, LinkedHashMap::new, Collectors.toList()));
        List<Map<String, Object>> rows = new ArrayList<>();

        if (isSyph) {
            // OrderByNameAsc — гарантированный порядок МКБ-кодов
            List<Diagnosis> diags = diagnosisRepository
                    .findByDiagnosisGroupIdOrderByNameAsc(diagGroupId);

            for (var entry : grouped.entrySet()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", entry.getKey());
                int rowTotal = 0;
                for (Diagnosis d : diags) {
                    long cnt = entry.getValue().stream()
                            .filter(c -> c.getDiagnosis().getId().equals(d.getId()))
                            .count();
                    row.put(d.getName().split(" ")[0], cnt > 0 ? (int) cnt : null);
                    rowTotal += cnt;
                }
                row.put("Всего", rowTotal > 0 ? rowTotal : null);
                rows.add(row);
            }
            addSyphilisTotals(rows, cases, diags);
        } else {
            for (var entry : grouped.entrySet()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", entry.getKey());
                long m = entry.getValue().stream().filter(this::isMale).count();
                long f = entry.getValue().size() - m;
                int t = entry.getValue().size();
                row.put("male", m > 0 ? (int) m : null);
                row.put("female", f > 0 ? (int) f : null);
                row.put("total", t);
                row.put("percent", totalForPercent > 0
                        ? Math.round((double) t / totalForPercent * 1000.0) / 10.0
                        : null);
                rows.add(row);
            }
            Map<String, Object> itogo = new LinkedHashMap<>();
            itogo.put("name", "ИТОГО");
            long m = cases.stream().filter(this::isMale).count();
            itogo.put("male", m > 0 ? (int) m : null);
            itogo.put("female", cases.size()-m > 0 ? (int)(cases.size()-m) : null);
            itogo.put("total", cases.size());
            itogo.put("percent", 100);
            rows.add(itogo);
        }
        return rows;
    }

    /** Итоговые строки для формата Сифилиса */
    private void addSyphilisTotals(List<Map<String, Object>> rows,
                                   List<DetectionCase> cases,
                                   List<Diagnosis> diags) {
        for (String gc : new String[]{GENDER_MALE, GENDER_FEMALE}) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", GENDER_MALE.equals(gc)
                    ? "Всего: Мужчины" : "Всего: Женщины");
            int rt = 0;
            for (Diagnosis d : diags) {
                long cnt = cases.stream()
                        .filter(c -> c.getDiagnosis().getId().equals(d.getId())
                                && c.getPatient().getGender().getCode().equals(gc))
                        .count();
                row.put(d.getName().split(" ")[0], cnt > 0 ? (int) cnt : null);
                rt += cnt;
            }
            row.put("Всего", rt > 0 ? rt : null);
            rows.add(row);
        }
        Map<String, Object> itogo = new LinkedHashMap<>();
        itogo.put("name", "ИТОГО");
        int t = 0;
        for (Diagnosis d : diags) {
            long cnt = cases.stream()
                    .filter(c -> c.getDiagnosis().getId().equals(d.getId())).count();
            itogo.put(d.getName().split(" ")[0], cnt > 0 ? (int) cnt : null);
            t += cnt;
        }
        itogo.put("Всего", t > 0 ? t : null);
        rows.add(itogo);
    }

    /** Секция "Район проживания" с подытогами город/край */
    private List<Map<String, Object>> buildDistrictSection(
            List<DetectionCase> cases, boolean isSyph, int year,
            Long diagGroupId) {

        Map<Long, Integer> popByState = new HashMap<>();
        for (Population p : populationRepository.findByYear(year)) {
            popByState.put(p.getState().getId(), p.getCountAll());
        }

        Map<String, List<DetectionCase>> byDistrict = cases.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getState().getName(),
                        LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> rows = new ArrayList<>();
        List<DetectionCase> cityCases = new ArrayList<>();
        List<DetectionCase> regionCases = new ArrayList<>();

        for (var entry : byDistrict.entrySet()) {
            List<DetectionCase> dc = entry.getValue();
            if (isCityDistrict(dc.get(0))) cityCases.addAll(dc);
            else regionCases.addAll(dc);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", entry.getKey());
            long m = dc.stream().filter(this::isMale).count();
            row.put("male", m > 0 ? (int) m : null);
            row.put("female", dc.size()-m > 0 ? (int)(dc.size()-m) : null);
            row.put("total", dc.size());

            Long stId = dc.get(0).getState().getId();
            Integer pop = popByState.get(stId);
            row.put("per100k", pop != null && pop > 0
                    ? Math.round((double)dc.size()*100000/pop*10.0)/10.0 : null);
            rows.add(row);
        }

        addSubtotalRow(rows, "Итого по Красноярску", cityCases);
        addSubtotalRow(rows, "Итого по Краю", regionCases);
        addSubtotalRow(rows, "ИТОГО", cases);
        return rows;
    }

    private void addSubtotalRow(List<Map<String, Object>> rows,
                                String label, List<DetectionCase> cases) {
        if (cases.isEmpty() && !"ИТОГО".equals(label)) return;
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", label);
        long m = cases.stream().filter(this::isMale).count();
        row.put("male", m > 0 ? (int) m : null);
        row.put("female", cases.size()-m > 0 ? (int)(cases.size()-m) : null);
        row.put("total", cases.size());
        row.put("per100k", null);
        rows.add(row);
    }

    /** Секция "Возрастная группа" */
    private List<Map<String, Object>> buildAgeGroupSection(
            List<DetectionCase> cases, boolean isSyph, Long diagGroupId) {

        int[][] ranges = {{0,14},{15,17},{18,19},{20,29},{30,39},{40,200}};
        String[] names = {"0-14 лет","15-17 лет","18-19 лет",
                "20-29 лет","30-39 лет","40 лет и старше"};
        int totalCount = cases.size();
        List<Diagnosis> diags = isSyph
                ? diagnosisRepository.findByDiagnosisGroupIdOrderByNameAsc(diagGroupId)
                : null;

        List<Map<String, Object>> rows = new ArrayList<>();

        for (int i = 0; i < ranges.length; i++) {
            List<DetectionCase> ac = filterByAge(cases, ranges[i][0], ranges[i][1]);
            if (ac.isEmpty()) continue;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", names[i]);

            if (isSyph && diags != null) {
                int rt = 0;
                for (Diagnosis d : diags) {
                    long cnt = ac.stream()
                            .filter(c -> c.getDiagnosis().getId().equals(d.getId()))
                            .count();
                    row.put(d.getName().split(" ")[0], cnt > 0 ? (int) cnt : null);
                    rt += cnt;
                }
                row.put("Всего", rt > 0 ? rt : null);
            } else {
                long m = ac.stream().filter(this::isMale).count();
                row.put("male", m > 0 ? (int) m : null);
                row.put("female", ac.size()-m > 0 ? (int)(ac.size()-m) : null);
                row.put("total", ac.size());
                row.put("percent", totalCount > 0
                        ? Math.round((double)ac.size()/totalCount*1000.0)/10.0 : null);
            }
            rows.add(row);
        }

        // ИТОГО
        Map<String, Object> itogo = new LinkedHashMap<>();
        itogo.put("name", "ИТОГО");
        if (isSyph && diags != null) {
            int t = 0;
            for (Diagnosis d : diags) {
                long cnt = cases.stream()
                        .filter(c -> c.getDiagnosis().getId().equals(d.getId())).count();
                itogo.put(d.getName().split(" ")[0], cnt > 0 ? (int) cnt : null);
                t += cnt;
            }
            itogo.put("Всего", t > 0 ? t : null);
        } else {
            long m = cases.stream().filter(this::isMale).count();
            itogo.put("male", m > 0 ? (int) m : null);
            itogo.put("female", cases.size()-m > 0 ? (int)(cases.size()-m) : null);
            itogo.put("total", cases.size());
            itogo.put("percent", 100);
        }
        rows.add(itogo);
        return rows;
    }



}
