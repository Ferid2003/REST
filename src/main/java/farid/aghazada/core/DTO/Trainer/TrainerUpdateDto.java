package farid.aghazada.core.DTO.Trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import farid.aghazada.core.Entity.TrainingType;


public record TrainerUpdateDto(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "First name cannot be blank")
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        String lastName,
        @NotNull(message = "Specializations are required")
        TrainingType specialization,
        @NotNull(message = "Active status is required")
        Boolean isActive
) {
}
