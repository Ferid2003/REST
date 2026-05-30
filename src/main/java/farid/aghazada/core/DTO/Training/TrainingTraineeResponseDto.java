package farid.aghazada.core.DTO.Training;

import java.time.LocalDate;

import farid.aghazada.core.Entity.Training;
import farid.aghazada.core.Entity.TrainingType;

public record TrainingTraineeResponseDto(
        String trainingName,
        LocalDate trainingDate,
        TrainingType trainingType,
        Long trainingDuration,
        String trainerName
) {
    public static TrainingTraineeResponseDto toTrainingResponseDto(Training training) {
        return new TrainingTraineeResponseDto(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType(),
                training.getTrainingDuration(),
                training.getTrainer().getUser().getUsername()
                );
    }
}
