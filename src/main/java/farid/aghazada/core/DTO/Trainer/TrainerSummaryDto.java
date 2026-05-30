package farid.aghazada.core.DTO.Trainer;

import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.TrainingType;

public record TrainerSummaryDto(
    String username,
    String firstName,
    String lastName,
    TrainingType specialization
) {
    public static TrainerSummaryDto toTrainerSummaryDto(Trainer trainer) {
        return new TrainerSummaryDto(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization()
                );
    }
}
