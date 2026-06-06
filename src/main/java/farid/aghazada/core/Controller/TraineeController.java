package farid.aghazada.core.Controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import farid.aghazada.core.DTO.Trainee.TraineeProfileResponseDto;
import farid.aghazada.core.DTO.Trainee.TraineeRegistrationDto;
import farid.aghazada.core.DTO.Trainee.TraineeTrainerUpdateDto;
import farid.aghazada.core.DTO.Trainee.TraineeUpdateDto;
import farid.aghazada.core.DTO.Trainee.TraineeUpdateProfileResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerSummaryDto;
import farid.aghazada.core.DTO.Training.TrainingTraineeCriteriaDto;
import farid.aghazada.core.DTO.Training.TrainingTraineeResponseDto;
import farid.aghazada.core.Service.AuthenticationService;
import farid.aghazada.core.Service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Trainee API", description = "REST API for managing trainees")
public class TraineeController {
    
    private final TraineeService traineeService;
    private final AuthenticationService authenticationService;

    public TraineeController(TraineeService traineeService, AuthenticationService authenticationService) {
        this.traineeService = traineeService;
        this.authenticationService = authenticationService;
    }


    @Operation(
        summary = "Register a new trainee",
        description = "Creates a new trainee profile. Username and password are auto-generated."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainee registered successfully — returns generated username and password"),
        @ApiResponse(responseCode = "400", description = "Validation error — firstName or lastName is blank")
    })
    @PostMapping("/trainees")
    public ResponseEntity<RegistrationResponseDto> createTrainee(
            @Valid @RequestBody TraineeRegistrationDto dto
    ) {
        return ResponseEntity.ok(traineeService.createTrainee(dto));
    }

    @Operation(
        summary = "Trainee login",
        description = "Validates trainee credentials. Returns 200 OK if valid, 400 if invalid."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/trainees/authenticate")
    public ResponseEntity<Void> authenticateTrainee(
            @Valid @RequestBody AuthenticationRequestDto dto
    ) {
        boolean authenticated = authenticationService.authenticateTrainee(dto.username(), dto.password());
        if (authenticated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(
        summary = "Get trainee profile",
        description = "Returns full profile of a trainee including their assigned trainers list. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainee profile returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/trainees/{username}")
    public ResponseEntity<TraineeProfileResponseDto> getTraineeByUsername(
            @Parameter(description = "Trainee username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainee password for authentication", required = true)
            @RequestHeader("X-Password") String password
    ) {
        return ResponseEntity.ok(traineeService.getTraineeByUsername(username, password));
    }

    @Operation(
        summary = "Change trainee password",
        description = "Updates the password for a trainee. Old password must be provided for verification. Requires authentication via X-Password header."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Old password does not match"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PutMapping("/trainees/{username}/password")
    public ResponseEntity<Void> changeTraineePassword(
            @Parameter(description = "Trainee username", required = true)
            @PathVariable String username,
            @Parameter(description = "Current password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody PasswordChangeDto dto
    ) {
        traineeService.changePassword(username, password, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Update trainee profile",
        description = "Updates mutable fields of a trainee profile. Username cannot be changed. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainee profile updated — returns updated profile with trainers list"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PutMapping("/trainees/{username}")
    public ResponseEntity<TraineeUpdateProfileResponseDto> updateTrainee(
            @Parameter(description = "Trainee username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainee password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody TraineeUpdateDto dto
    ) {
        return ResponseEntity.ok(traineeService.updateTrainee(username, password, dto));
    }

    @Operation(
        summary = "Activate or deactivate a trainee",
        description = "Toggles the active status of a trainee. This action is NOT idempotent — activating an already-active trainee is an error. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Trainee is already in the requested state"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PatchMapping("/trainees/{username}/activate/{isActive}")
    public ResponseEntity<Void> activateDeactivateTrainee(
            @Parameter(description = "Trainee username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainee password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @Parameter(description = "Desired active status: true to activate, false to deactivate", required = true)
            @PathVariable boolean isActive
    ) {
        traineeService.changeIsActive(username, password, isActive);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Delete trainee profile",
        description = "Hard deletes a trainee and all their associated trainings (cascade). Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainee deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @DeleteMapping("/trainees/{username}")
    public ResponseEntity<Void> deleteTrainee(
            @Parameter(description = "Trainee username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainee password for authentication", required = true)
            @RequestHeader("X-Password") String password
    ) {
        traineeService.deleteTraineeByUsername(username, password);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get unassigned active trainers for a trainee",
        description = "Returns a list of active trainers who are not yet assigned to the given trainee. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of unassigned trainers returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/trainees/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryDto>> getUnassignedTrainers(
            @Parameter(description = "Trainee username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainee password for authentication", required = true)
            @RequestHeader("X-Password") String password
    ) {
        return ResponseEntity.ok(traineeService.getUnassignedTrainers(username, password));
    }

}
