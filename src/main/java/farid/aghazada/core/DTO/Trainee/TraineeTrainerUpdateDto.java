package farid.aghazada.core.DTO.Trainee;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record TraineeTrainerUpdateDto(
        @NotNull(message = "Trainer usernames list is required")
        List<String> trainerUsernames
) {
}
