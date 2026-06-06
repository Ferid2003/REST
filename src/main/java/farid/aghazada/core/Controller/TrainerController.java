package farid.aghazada.core.Controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import farid.aghazada.core.DTO.AuthenticationRequestDto;
import farid.aghazada.core.DTO.PasswordChangeDto;
import farid.aghazada.core.DTO.RegistrationResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerProfileResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerRegistrationDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateProfileResponseDto;
import farid.aghazada.core.Service.AuthenticationService;
import farid.aghazada.core.Service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Trainer API", description = "REST API for managing trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final AuthenticationService authenticationService;

    public TrainerController(TrainerService trainerService, AuthenticationService authenticationService) {
        this.trainerService = trainerService;
        this.authenticationService = authenticationService;
    }

    @Operation(
        summary = "Register a new trainer",
        description = "Creates a new trainer profile. Username and password are auto-generated."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainer registered successfully — returns generated username and password"),
        @ApiResponse(responseCode = "400", description = "Validation error — required field missing or training type not found")
    })
    @PostMapping("/trainers")
    public ResponseEntity<RegistrationResponseDto> createTrainer(
            @Valid @RequestBody TrainerRegistrationDto dto
    ) {
        return ResponseEntity.ok(trainerService.createTrainer(dto));
    }

    @Operation(
        summary = "Trainer login",
        description = "Validates trainer credentials. Returns 200 OK if valid, 400 if invalid."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/trainers/authenticate")
    public ResponseEntity<Void> authenticateTrainer(
            @Valid @RequestBody AuthenticationRequestDto dto
    ) {
        boolean authenticated = authenticationService.authenticateTrainer(dto.username(), dto.password());
        if (authenticated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(
        summary = "Get trainer profile",
        description = "Returns full profile of a trainer including their trainees list. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainer profile returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/trainers/{username}")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerByUsername(
            @Parameter(description = "Trainer username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainer password for authentication", required = true)
            @RequestHeader("X-Password") String password
    ) {
        return ResponseEntity.ok(trainerService.getTrainerByUsername(username, password));
    }

    @Operation(
        summary = "Change trainer password",
        description = "Updates the password for a trainer. Old password must be provided for verification. Requires authentication via X-Password header."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Old password does not match"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PutMapping("/trainers/{username}/password")
    public ResponseEntity<Void> changeTrainerPassword(
            @Parameter(description = "Trainer username", required = true)
            @PathVariable String username,
            @Parameter(description = "Current password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody PasswordChangeDto dto
    ) {
        trainerService.changePassword(username, password, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Update trainer profile",
        description = "Updates mutable fields of a trainer profile. Specialization is read-only. Username cannot be changed. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainer profile updated — returns updated profile with trainees list"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PutMapping("/trainers/{username}")
    public ResponseEntity<TrainerUpdateProfileResponseDto> updateTrainer(
            @Parameter(description = "Trainer username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainer password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody TrainerUpdateDto dto
    ) {
        return ResponseEntity.ok(trainerService.updateTrainer(username, password, dto));
    }

    @Operation(
        summary = "Activate or deactivate a trainer",
        description = "Toggles the active status of a trainer. This action is NOT idempotent — activating an already-active trainer is an error. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Trainer is already in the requested state"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PatchMapping("/trainers/{username}/activate/{isActive}")
    public ResponseEntity<Void> activateDeactivateTrainer(
            @Parameter(description = "Trainer username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainer password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @Parameter(description = "Desired active status: true to activate, false to deactivate", required = true)
            @PathVariable boolean isActive
    ) {
        trainerService.changeIsActive(username, password, isActive);
        return ResponseEntity.ok().build();
    }
}
