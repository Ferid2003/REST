package farid.aghazada.core.DTO.Training;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import farid.aghazada.core.Entity.Trainee;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.Training;

public record TrainingCreationDto(
        @NotBlank(message = "Trainee username cannot be blank")
        String traineeUsername,
        @NotBlank(message = "Trainer username cannot be blank")
        String trainerUsername,
        @NotBlank(message = "Training name cannot be blank")
        String name,
        @NotNull(message = "Training date is required")
        LocalDate date,
        @NotNull(message = "Training duration is required")
        Long duration
) {
    
    public static Training toTraining(TrainingCreationDto dto, Trainer trainer, Trainee trainee) {
        Training training = new Training();
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingName(dto.name());
        training.setTrainingDate(dto.date());
        training.setTrainingDuration(dto.duration());
        training.setTrainingType(trainer.getSpecialization());

        return training;
    }
}
