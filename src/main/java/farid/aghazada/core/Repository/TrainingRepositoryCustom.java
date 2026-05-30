package farid.aghazada.core.Repository;

import java.time.LocalDate;
import java.util.List;

import farid.aghazada.core.Entity.Training;

public interface TrainingRepositoryCustom {

    List<Training> findTraineeTrainingByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName
            );

    List<Training> findTrainerTrainingByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    );
}
