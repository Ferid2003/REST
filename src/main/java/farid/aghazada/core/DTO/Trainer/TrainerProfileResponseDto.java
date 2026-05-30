package farid.aghazada.core.DTO.Trainer;

import java.util.Set;
import java.util.stream.Collectors;

import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.TrainingType;
import farid.aghazada.core.DTO.Trainee.TraineeSummaryDto;

public record TrainerProfileResponseDto(
        String firstName,
        String lastName,
        TrainingType specialization,
        Boolean isActive,
        Set<TraineeSummaryDto> trainees
) {
    public static TrainerProfileResponseDto toTrainerProfileResponseDto(Trainer trainer) {
        return new TrainerProfileResponseDto(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization(),
                trainer.getUser().isActive(),
                trainer.getTrainees().stream()
                    .map(TraineeSummaryDto::toTraineeSummaryDto)
                    .collect(Collectors.toSet())
                );
    }
}
