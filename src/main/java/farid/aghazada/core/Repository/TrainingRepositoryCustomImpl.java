package farid.aghazada.core.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import farid.aghazada.core.Entity.Trainee;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.Training;
import farid.aghazada.core.Entity.TrainingType;
import farid.aghazada.core.Entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class TrainingRepositoryCustomImpl implements TrainingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Training> findTraineeTrainingByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);

        Join<Training, Trainee> traineeJoin = training.join("trainee");
        Join<Trainee, User> traineeUserJoin = traineeJoin.join("user");
        Join<Training, Trainer> trainerJoin = training.join("trainer");
        Join<Trainer, User> trainerUserJoin = trainerJoin.join("user");
        Join<Training, TrainingType> trainingTypeJoin = training.join("trainingType");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(traineeUserJoin.get("username"), traineeUsername));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }

        if (trainerName != null && !trainerName.isBlank()) {
            String normalizedTrainerName = "%" + trainerName.toLowerCase() + "%";
            Predicate firstNameLike = cb.like(cb.lower(trainerUserJoin.get("firstName")), normalizedTrainerName);
            Predicate lastNameLike = cb.like(cb.lower(trainerUserJoin.get("lastName")), normalizedTrainerName);
            Predicate fullNameLike = cb.like(
                    cb.lower(
                            cb.concat(
                                    cb.concat(trainerUserJoin.get("firstName"), " "),
                                    trainerUserJoin.get("lastName")
                            )
                    ),
                    normalizedTrainerName
            );
            predicates.add(cb.or(firstNameLike, lastNameLike, fullNameLike));
        }

        if (trainingTypeName != null && !trainingTypeName.isBlank()) {
            predicates.add(cb.equal(cb.lower(trainingTypeJoin.get("name")), trainingTypeName.toLowerCase()));
        }

        cq.select(training)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(training.get("trainingDate")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Training> findTrainerTrainingByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);

        Join<Training, Trainer> trainerJoin = training.join("trainer");
        Join<Trainer, User> trainerUserJoin = trainerJoin.join("user");
        Join<Training, Trainee> traineeJoin = training.join("trainee");
        Join<Trainee, User> traineeUserJoin = traineeJoin.join("user");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trainerUserJoin.get("username"), trainerUsername));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }

        if (traineeName != null && !traineeName.isBlank()) {
            String normalizedTraineeName = "%" + traineeName.toLowerCase() + "%";
            Predicate firstNameLike = cb.like(cb.lower(traineeUserJoin.get("firstName")), normalizedTraineeName);
            Predicate lastNameLike = cb.like(cb.lower(traineeUserJoin.get("lastName")), normalizedTraineeName);
            Predicate fullNameLike = cb.like(
                    cb.lower(
                            cb.concat(
                                    cb.concat(traineeUserJoin.get("firstName"), " "),
                                    traineeUserJoin.get("lastName")
                            )
                    ),
                    normalizedTraineeName
            );
            predicates.add(cb.or(firstNameLike, lastNameLike, fullNameLike));
        }

        cq.select(training)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(training.get("trainingDate")));

        return entityManager.createQuery(cq).getResultList();
    }
}
