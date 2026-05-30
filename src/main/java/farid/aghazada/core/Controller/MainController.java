package farid.aghazada.core.Controller;

import java.util.List;

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
import farid.aghazada.core.DTO.Training.TrainingCreationDto;
import farid.aghazada.core.DTO.Trainee.TraineeProfileResponseDto;
import farid.aghazada.core.DTO.Trainee.TraineeRegistrationDto;
import farid.aghazada.core.DTO.Trainee.TraineeTrainerUpdateDto;
import farid.aghazada.core.DTO.Trainee.TraineeUpdateDto;
import farid.aghazada.core.DTO.Trainee.TraineeUpdateProfileResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerProfileResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerRegistrationDto;
import farid.aghazada.core.DTO.Trainer.TrainerSummaryDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateProfileResponseDto;
import farid.aghazada.core.DTO.Training.TrainingTraineeCriteriaDto;
import farid.aghazada.core.DTO.Training.TrainingTraineeResponseDto;
import farid.aghazada.core.DTO.Training.TrainingTrainerCriteriaDto;
import farid.aghazada.core.DTO.Training.TrainingTrainerResponseDto;
import farid.aghazada.core.Entity.TrainingType;
import farid.aghazada.core.Service.AuthenticationService;
import farid.aghazada.core.Service.TraineeService;
import farid.aghazada.core.Service.TrainerService;
import farid.aghazada.core.Service.TrainingService;
import farid.aghazada.core.Service.TrainingTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Gym CRM API", description = "REST API for managing trainers, trainees and trainings")
public class MainController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final AuthenticationService authenticationService;

    public MainController(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService, AuthenticationService authenticationService, TrainingTypeService trainingTypeService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.authenticationService = authenticationService;
    }

    // ─── Trainee Registration ────────────────────────────────────────────────────

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

    // ─── Trainer Registration ────────────────────────────────────────────────────

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

    // ─── Login ───────────────────────────────────────────────────────────────────

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

    // ─── Get Trainee Profile ─────────────────────────────────────────────────────

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

    // ─── Get Trainer Profile ─────────────────────────────────────────────────────

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

    // ─── Change Password ─────────────────────────────────────────────────────────

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

    // ─── Update Trainee Profile ───────────────────────────────────────────────────

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

    // ─── Update Trainer Profile ───────────────────────────────────────────────────

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

    // ─── Activate / De-Activate ───────────────────────────────────────────────────

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

    // ─── Delete Trainee ───────────────────────────────────────────────────────────

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

    // ─── Add Training ─────────────────────────────────────────────────────────────

    @Operation(
        summary = "Add a new training",
        description = "Creates a new training session linking a trainee and a trainer. Provide credentials of the requesting user via X-Username and X-Password headers. Training cannot be updated or deleted via REST."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Training created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error — required field missing"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    })
    @PostMapping("/trainings")
    public ResponseEntity<Void> createTraining(
            @Parameter(description = "Username of the authenticated user", required = true)
            @RequestHeader("X-Username") String username,
            @Parameter(description = "Password of the authenticated user", required = true)
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody TrainingCreationDto dto
    ) {
        trainingService.createTraining(username, password, dto);
        return ResponseEntity.ok().build();
    }

    // ─── Trainee Trainings List ───────────────────────────────────────────────────

    @Operation(
        summary = "Get trainee trainings list",
        description = "Returns trainings for a trainee, optionally filtered by date range, trainer name, and training type. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainings list returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/trainees/{username}/trainings/search")
    public ResponseEntity<List<TrainingTraineeResponseDto>> traineeTrainings(
            @Parameter(description = "Trainee username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainee password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @RequestBody(required = false) TrainingTraineeCriteriaDto criteria
    ) {
        return ResponseEntity.ok(traineeService.getTrainingsByCriteria(username, password, criteria));
    }

    // ─── Trainer Trainings List ───────────────────────────────────────────────────

    @Operation(
        summary = "Get trainer trainings list",
        description = "Returns trainings for a trainer, optionally filtered by date range and trainee name. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainings list returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/trainers/{username}/trainings/search")
    public ResponseEntity<List<TrainingTrainerResponseDto>> trainerTrainings(
            @Parameter(description = "Trainer username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainer password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @RequestBody(required = false) TrainingTrainerCriteriaDto criteria
    ) {
        return ResponseEntity.ok(trainerService.getTrainingsByCriteria(username, password, criteria));
    }

    // ─── Unassigned Trainers ──────────────────────────────────────────────────────

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

    // ─── Update Trainee's Trainer List ────────────────────────────────────────────

    @Operation(
        summary = "Update trainee's trainer list",
        description = "Replaces the full list of trainers assigned to a trainee. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainer list updated — returns new trainers list"),
        @ApiResponse(responseCode = "400", description = "Trainers list is null or a trainer username was not found"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PutMapping("/trainees/{username}/trainers")
    public ResponseEntity<List<TrainerSummaryDto>> updateTraineeTrainers(
            @Parameter(description = "Trainee username", required = true)
            @PathVariable String username,
            @Parameter(description = "Trainee password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @Valid @RequestBody TraineeTrainerUpdateDto dto
    ) {
        return ResponseEntity.ok(traineeService.updateTrainerList(username, password, dto));
    }

    // ─── Training Types ───────────────────────────────────────────────────────────

    @Operation(
        summary = "Get all training types",
        description = "Returns the full list of available training types. This list is constant and cannot be modified via the API. Requires authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Training types list returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @GetMapping("/training-types")
    public ResponseEntity<List<TrainingType>> getTrainingTypes(
            @Parameter(description = "Username of the authenticated user", required = true)
            @RequestHeader("X-Username") String username,
            @Parameter(description = "Password of the authenticated user", required = true)
            @RequestHeader("X-Password") String password
    ) {
        return ResponseEntity.ok(trainingTypeService.getAllTrainingTypes(username, password));
    }
}
