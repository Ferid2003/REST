package farid.aghazada.core.DTO;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeDto(
        @NotBlank(message = "Old password cannot be blank")
        String oldPassword,
        @NotBlank(message = "New password cannot be blank")
        String newPassword
) {
}
