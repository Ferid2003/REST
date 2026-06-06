package farid.aghazada.core.Controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import farid.aghazada.core.DTO.Trainee.TraineeTrainerUpdateDto;
import farid.aghazada.core.DTO.Trainer.TrainerSummaryDto;
import farid.aghazada.core.DTO.Training.TrainingCreationDto;
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

@RestController
@RequestMapping("/api")
@Tag(name = "Training API", description = "REST API for managing trainings")
public class TrainingController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final AuthenticationService authenticationService;

    public TrainingController(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService, AuthenticationService authenticationService, TrainingTypeService trainingTypeService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.authenticationService = authenticationService;
    }


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
