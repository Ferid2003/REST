package farid.aghazada.core.DTO.Trainee;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TraineeUpdateDto(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "First name cannot be blank")
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        String lastName,
        LocalDate dateOfBirth,
        String address,
        @NotNull(message = "Active status is required")
        Boolean isActive
) {
}
