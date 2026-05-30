package farid.aghazada.core.DTO.Trainee;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import farid.aghazada.core.DTO.Trainer.TrainerSummaryDto;

import farid.aghazada.core.Entity.Trainee;

public record TraineeProfileResponseDto(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        Boolean isActive,
        Set<TrainerSummaryDto> trainers
) {

    public static TraineeProfileResponseDto toTrainerProfileResponseDto(Trainee trainee) {
        return new TraineeProfileResponseDto(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainee.getTrainers().stream()
                    .map(TrainerSummaryDto::toTrainerSummaryDto)
                    .collect(Collectors.toSet())
                );
    }

}
