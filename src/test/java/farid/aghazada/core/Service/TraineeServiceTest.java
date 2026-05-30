package farid.aghazada.core.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import farid.aghazada.core.Entity.Trainee;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.User;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.TraineeRepository;
import farid.aghazada.core.Repository.TrainerRepository;
import farid.aghazada.core.Repository.TrainingRepository;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private HelperService helperService;

    @InjectMocks
    private TraineeService traineeService;

    // ─── createTrainee ────────────────────────────────────────────────────────────

    @Test
    void createTraineeGeneratesCredentialsAndSaves() {
        TraineeRegistrationDto dto = new TraineeRegistrationDto("Jane", "Doe", LocalDate.of(2000, 1, 1), "Baku");

        when(helperService.generateUsername("Jane", "Doe")).thenReturn("Jane.Doe");
        when(helperService.generatePassword()).thenReturn("pass123456");

        RegistrationResponseDto result = traineeService.createTrainee(dto);

        assertThat(result.username()).isEqualTo("Jane.Doe");
        assertThat(result.password()).isEqualTo("pass123456");
        verify(traineeRepository).save(any(Trainee.class));
    }

    // ─── authenticate ─────────────────────────────────────────────────────────────

    @Test
    void authenticateReturnsTrueWhenTraineeExists() {
        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(new Trainee()));

        boolean result = traineeService.authenticate("Jane.Doe", "pw");

        assertThat(result).isTrue();
    }

    @Test
    void authenticateReturnsFalseWhenTraineeNotFound() {
        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.empty());

        boolean result = traineeService.authenticate("Jane.Doe", "pw");

        assertThat(result).isFalse();
    }

    // ─── getTraineeByUsername ─────────────────────────────────────────────────────

    @Test
    void getTraineeByUsernameReturnsProfileDto() {
        User user = new User();
        user.setUsername("Jane.Doe");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setActive(true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setTrainers(new HashSet<>());

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

        TraineeProfileResponseDto result = traineeService.getTraineeByUsername("Jane.Doe", "pw");

        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getTraineeByUsernameThrowsWhenNotFound() {
        when(traineeRepository.findByUserUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getTraineeByUsername("missing", "pw"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── updateTrainee ────────────────────────────────────────────────────────────

    @Test
    void updateTraineeUpdatesMutableFields() {
        User user = new User();
        user.setUsername("Jane.Doe");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setActive(true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setTrainers(new HashSet<>());

        // username=null so it won't try to change username
        TraineeUpdateDto dto = new TraineeUpdateDto(null, "Janet", "Dane", LocalDate.of(1999, 3, 5), "New addr", false);

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

        TraineeUpdateProfileResponseDto result = traineeService.updateTrainee("Jane.Doe", "pw", dto);

        assertThat(result.firstName()).isEqualTo("Janet");
        assertThat(result.lastName()).isEqualTo("Dane");
        assertThat(result.address()).isEqualTo("New addr");
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void updateTraineeThrowsWhenNotFound() {
        when(traineeRepository.findByUserUsername("missing")).thenReturn(Optional.empty());

        TraineeUpdateDto dto = new TraineeUpdateDto(null, "Janet", "Dane", null, null, true);

        assertThatThrownBy(() -> traineeService.updateTrainee("missing", "pw", dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── changePassword ───────────────────────────────────────────────────────────

    @Test
    void changePasswordUpdatesWhenOldPasswordMatches() {
        User user = new User();
        user.setPassword("old");
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

        traineeService.changePassword("Jane.Doe", "old", new PasswordChangeDto("old", "newPass"));

        assertThat(trainee.getUser().getPassword()).isEqualTo("newPass");
    }

    @Test
    void changePasswordThrowsWhenOldPasswordMismatched() {
        User user = new User();
        user.setPassword("old");
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.changePassword("Jane.Doe", "old", new PasswordChangeDto("wrong", "newPass")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Old password does not match");
    }

    // ─── changeIsActive ───────────────────────────────────────────────────────────

    @Test
    void changeIsActiveActivatesInactiveTrainee() {
        User user = new User();
        user.setActive(false);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

        traineeService.changeIsActive("Jane.Doe", "pw", true);

        assertThat(trainee.getUser().isActive()).isTrue();
    }

    @Test
    void changeIsActiveThrowsWhenAlreadyInRequestedState() {
        User user = new User();
        user.setActive(true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> traineeService.changeIsActive("Jane.Doe", "pw", true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already active");
    }

    // ─── deleteTraineeByUsername ──────────────────────────────────────────────────

    @Test
    void deleteTraineeByUsernameDeletesEntity() {
        User user = new User();
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

        traineeService.deleteTraineeByUsername("Jane.Doe", "pw");

        verify(traineeRepository).delete(trainee);
    }

    @Test
    void deleteTraineeThrowsWhenNotFound() {
        when(traineeRepository.findByUserUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.deleteTraineeByUsername("missing", "pw"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── getTrainingsByCriteria ───────────────────────────────────────────────────

    @Test
    void getTrainingsByCriteriaWithNullCriteriaReturnsEmptyList() {
        when(trainingRepository.findTraineeTrainingByCriteria("Jane.Doe", null, null, null, null))
                .thenReturn(List.of());

        List<TrainingTraineeResponseDto> result = traineeService.getTrainingsByCriteria("Jane.Doe", "pw", null);

        assertThat(result).isEmpty();
    }

    @Test
    void getTrainingsByCriteriaDelegatesToRepository() {
        TrainingTraineeCriteriaDto criteria = new TrainingTraineeCriteriaDto(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), "John", null);

        when(trainingRepository.findTraineeTrainingByCriteria(
                "Jane.Doe",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "John",
                null))
                .thenReturn(List.of());

        List<TrainingTraineeResponseDto> result = traineeService.getTrainingsByCriteria("Jane.Doe", "pw", criteria);

        assertThat(result).isEmpty();
    }

    // ─── getUnassignedTrainers ────────────────────────────────────────────────────

    @Test
    void getUnassignedTrainersDelegatesToRepository() {
        User trainerUser = new User();
        trainerUser.setUsername("John.Smith");
        trainerUser.setFirstName("John");
        trainerUser.setLastName("Smith");
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        when(trainerRepository.findTrainersNotAssignedToTrainee("Jane.Doe")).thenReturn(List.of(trainer));

        List<TrainerSummaryDto> result = traineeService.getUnassignedTrainers("Jane.Doe", "pw");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("John.Smith");
    }

    // ─── updateTrainerList ────────────────────────────────────────────────────────

    @Test
    void updateTrainerListReplacesTrainerSet() {
        User user = new User();
        user.setUsername("Jane.Doe");
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setTrainers(new HashSet<>());

        User trainerUser = new User();
        trainerUser.setUsername("John.Smith");
        trainerUser.setFirstName("John");
        trainerUser.setLastName("Smith");
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsernameIn(List.of("John.Smith"))).thenReturn(List.of(trainer));

        List<TrainerSummaryDto> result = traineeService.updateTrainerList(
                "Jane.Doe", "pw", new TraineeTrainerUpdateDto(List.of("John.Smith")));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("John.Smith");
    }

    @Test
    void updateTrainerListWithEmptyListClearsTrainers() {
        User user = new User();
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setTrainers(new HashSet<>());

        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

        List<TrainerSummaryDto> result = traineeService.updateTrainerList(
                "Jane.Doe", "pw", new TraineeTrainerUpdateDto(List.of()));

        assertThat(result).isEmpty();
    }
}
