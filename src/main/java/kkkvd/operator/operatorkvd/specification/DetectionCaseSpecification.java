package kkkvd.operator.operatorkvd.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import kkkvd.operator.operatorkvd.dto.PatientSearchRequest;
import kkkvd.operator.operatorkvd.entities.DetectionCase;
import kkkvd.operator.operatorkvd.entities.Patient;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DetectionCaseSpecification {
    public static Specification<DetectionCase> withFilters(PatientSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            if(query.getResultType() != Long.class && query.getResultType() != long.class){
                root.fetch("patient", JoinType.INNER).fetch("gender", JoinType.LEFT);
                root.fetch("diagnosis", JoinType.LEFT);
                root.fetch("doctor",  JoinType.LEFT);
                root.fetch("state", JoinType.LEFT);
            }

            Join<DetectionCase, Patient> patient = root.join("patient", JoinType.INNER);

            String lastName = normalize(request.getLastName());
            if (lastName != null) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(patient.get("lastName")),
                                lastName.toLowerCase() + "%"
                        )
                );
            }

            String firstName = normalize(request.getFirstName());
            if (firstName != null) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(patient.get("firstName")),
                                firstName.toLowerCase() + "%"
                        )
                );
            }

            String middleName = normalize(request.getMiddleName());
            if (middleName != null) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(patient.get("middleName")),
                                middleName.toLowerCase() + "%"
                        )
                );
            }

            if (request.getGenderId() != null) {
                predicates.add(
                        criteriaBuilder.equal(patient.get("gender").get("id"), request.getGenderId())
                );
            }

            if (request.getAgeFrom() != null) {
                LocalDate maxBirthDate = LocalDate.now().minusYears(request.getAgeFrom());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(patient.get("birthDate"), maxBirthDate));
            }

            if (request.getAgeTo() != null) {
                LocalDate minBirthDate = LocalDate.now()
                        .minusYears(request.getAgeTo() + 1)
                        .plusDays(1);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(patient.get("birthDate"), minBirthDate));
            }

            if (request.getStateId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("state").get("id"), request.getStateId()));
            }

            if (request.getDiagnosisId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("diagnosis").get("id"), request.getDiagnosisId()));
            }

            if (request.getDiagnosisGroupId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("diagnosis").get("diagnosisGroup").get("id"), request.getDiagnosisGroupId()
                ));
            }

            if (request.getDoctorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("doctor").get("id"), request.getDoctorId()));
            }

            if (request.getSocialGroupId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("socialGroup").get("id"), request.getSocialGroupId()));
            }

            if (request.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), request.getCreatedFrom().atStartOfDay()
                ));
            }

            if (request.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), request.getCreatedTo().atTime(LocalTime.MAX)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
