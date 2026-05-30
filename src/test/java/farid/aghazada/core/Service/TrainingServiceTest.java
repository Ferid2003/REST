package farid.aghazada.core.Service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import farid.aghazada.core.DTO.Training.TrainingCreationDto;
import farid.aghazada.core.Entity.Trainee;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.Training;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.TraineeRepository;
import farid.aghazada.core.Repository.TrainerRepository;
import farid.aghazada.core.Repository.TrainingRepository;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private TrainingService trainingService;

    // ─── createTraining ───────────────────────────────────────────────────────────

    @Test
    void createTrainingSavesWhenBothEntitiesExist() {
        TrainingCreationDto dto = new TrainingCreationDto(
                "jane.doe", "john.smith", "Morning Cardio", LocalDate.of(2026, 1, 1), 60L);

        Trainer trainer = new Trainer();
        Trainee trainee = new Trainee();

        when(trainerRepository.findByUserUsername("john.smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUserUsername("jane.doe")).thenReturn(Optional.of(trainee));

        trainingService.createTraining("john.smith", "pw", dto);

        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void createTrainingThrowsWhenTrainerNotFound() {
        TrainingCreationDto dto = new TrainingCreationDto(
                "jane.doe", "john.smith", "A", LocalDate.now(), 30L);

        when(trainerRepository.findByUserUsername("john.smith")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining("john.smith", "pw", dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("john.smith");
    }

    @Test
    void createTrainingThrowsWhenTraineeNotFound() {
        TrainingCreationDto dto = new TrainingCreationDto(
                "jane.doe", "john.smith", "A", LocalDate.now(), 30L);

        when(trainerRepository.findByUserUsername("john.smith")).thenReturn(Optional.of(new Trainer()));
        when(traineeRepository.findByUserUsername("jane.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining("john.smith", "pw", dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("jane.doe");
    }
}
