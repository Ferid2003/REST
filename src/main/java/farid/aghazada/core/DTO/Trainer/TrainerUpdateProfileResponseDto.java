package farid.aghazada.core.DTO.Trainer;

import java.util.Set;
import java.util.stream.Collectors;

import farid.aghazada.core.DTO.Trainee.TraineeSummaryDto;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.TrainingType;

public record TrainerUpdateProfileResponseDto(
        String username,
        String firstName,
        String lastName,
        TrainingType specialization,
        Boolean isActive,
        Set<TraineeSummaryDto> trainees
) {
    public static TrainerUpdateProfileResponseDto toTrainerUpdateProfileResponseDto(Trainer trainer) {
        return new TrainerUpdateProfileResponseDto(
                trainer.getUser().getUsername(),
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
