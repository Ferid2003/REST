package farid.aghazada.core.DTO.Trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.TrainingType;
import farid.aghazada.core.Entity.User;

public record TrainerRegistrationDto(
        @NotBlank(message = "First name cannot be blank")
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        String lastName,
        @NotNull(message = "Training type is required")
        TrainingType trainingType
) {

    public static Trainer toTrainer(TrainerRegistrationDto dto, String username, String password) {
        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(dto.trainingType());

        return trainer;
    }
}
