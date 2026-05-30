package farid.aghazada.core.DTO.Training;

import java.time.LocalDate;

import farid.aghazada.core.Entity.Training;
import farid.aghazada.core.Entity.TrainingType;

public record TrainingTrainerResponseDto(
        String trainingName,
        LocalDate trainingDate,
        TrainingType trainingType,
        Long trainingDuration,
        String traineeName
) {
    public static TrainingTrainerResponseDto toTrainingResponseDto(Training training) {
        return new TrainingTrainerResponseDto(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType(),
                training.getTrainingDuration(),
                training.getTrainee().getUser().getUsername()
                );
    }
}
