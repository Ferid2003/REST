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
import farid.aghazada.core.DTO.Trainer.TrainerProfileResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerRegistrationDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateProfileResponseDto;
import farid.aghazada.core.DTO.Training.TrainingTrainerCriteriaDto;
import farid.aghazada.core.DTO.Training.TrainingTrainerResponseDto;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.User;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.TrainerRepository;
import farid.aghazada.core.Repository.TrainingRepository;
import farid.aghazada.core.Repository.TrainingTypeRepository;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private HelperService helperService;

    @InjectMocks
    private TrainerService trainerService;

    // ─── createTrainer ────────────────────────────────────────────────────────────

    @Test
    void createTrainerGeneratesCredentialsAndSaves() {
        // Pass null trainingType to skip the trainingTypeRepository lookup branch
        TrainerRegistrationDto dto = new TrainerRegistrationDto("John", "Smith", null);

        when(helperService.generateUsername("John", "Smith")).thenReturn("John.Smith");
        when(helperService.generatePassword()).thenReturn("pass123456");

        RegistrationResponseDto result = trainerService.createTrainer(dto);

        assertThat(result.username()).isEqualTo("John.Smith");
        assertThat(result.password()).isEqualTo("pass123456");
        verify(trainerRepository).save(any(Trainer.class));
    }

    // ─── authenticate ─────────────────────────────────────────────────────────────

    @Test
    void authenticateReturnsTrueWhenTrainerExists() {
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(new Trainer()));

        boolean result = trainerService.authenticate("John.Smith", "pw");

        assertThat(result).isTrue();
    }

    @Test
    void authenticateReturnsFalseWhenTrainerNotFound() {
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.empty());

        boolean result = trainerService.authenticate("John.Smith", "pw");

        assertThat(result).isFalse();
    }

    // ─── getTrainerByUsername ─────────────────────────────────────────────────────

    @Test
    void getTrainerByUsernameReturnsProfileDto() {
        User user = new User();
        user.setUsername("John.Smith");
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setActive(true);
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setTrainees(new HashSet<>());

        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        TrainerProfileResponseDto result = trainerService.getTrainerByUsername("John.Smith", "pw");

        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getTrainerByUsernameThrowsWhenNotFound() {
        when(trainerRepository.findByUserUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainerByUsername("missing", "pw"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── updateTrainer ────────────────────────────────────────────────────────────

    @Test
    void updateTrainerUpdatesMutableFields() {
        User user = new User();
        user.setUsername("John.Smith");
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setActive(true);
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setTrainees(new HashSet<>());

        // specialization=null to skip trainingTypeRepository lookup
        TrainerUpdateDto dto = new TrainerUpdateDto(null, "Johnny", "S", null, false);

        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        TrainerUpdateProfileResponseDto result = trainerService.updateTrainer("John.Smith", "pw", dto);

        assertThat(result.firstName()).isEqualTo("Johnny");
        assertThat(result.lastName()).isEqualTo("S");
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void updateTrainerThrowsWhenNotFound() {
        when(trainerRepository.findByUserUsername("missing")).thenReturn(Optional.empty());

        TrainerUpdateDto dto = new TrainerUpdateDto(null, "Johnny", "S", null, true);

        assertThatThrownBy(() -> trainerService.updateTrainer("missing", "pw", dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── changePassword ───────────────────────────────────────────────────────────

    @Test
    void changePasswordUpdatesWhenOldMatches() {
        User user = new User();
        user.setPassword("old");
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        trainerService.changePassword("John.Smith", "old", new PasswordChangeDto("old", "newPass"));

        assertThat(trainer.getUser().getPassword()).isEqualTo("newPass");
    }

    @Test
    void changePasswordThrowsWhenOldMismatched() {
        User user = new User();
        user.setPassword("old");
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.changePassword("John.Smith", "old", new PasswordChangeDto("wrong", "newPass")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Old password does not match");
    }

    // ─── changeIsActive ───────────────────────────────────────────────────────────

    @Test
    void changeIsActiveActivatesInactiveTrainer() {
        User user = new User();
        user.setActive(false);
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        trainerService.changeIsActive("John.Smith", "pw", true);

        assertThat(trainer.getUser().isActive()).isTrue();
    }

    @Test
    void changeIsActiveThrowsWhenAlreadyInRequestedState() {
        User user = new User();
        user.setActive(true);
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.changeIsActive("John.Smith", "pw", true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already active");
    }

    // ─── getTrainingsByCriteria ───────────────────────────────────────────────────

    @Test
    void getTrainingsByCriteriaWithNullCriteriaReturnsEmptyList() {
        when(trainingRepository.findTrainerTrainingByCriteria("John.Smith", null, null, null))
                .thenReturn(List.of());

        List<TrainingTrainerResponseDto> result = trainerService.getTrainingsByCriteria("John.Smith", "pw", null);

        assertThat(result).isEmpty();
    }

    @Test
    void getTrainingsByCriteriaDelegatesToRepository() {
        TrainingTrainerCriteriaDto criteria = new TrainingTrainerCriteriaDto(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), "Jane", null);

        when(trainingRepository.findTrainerTrainingByCriteria(
                "John.Smith",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "Jane"))
                .thenReturn(List.of());

        List<TrainingTrainerResponseDto> result = trainerService.getTrainingsByCriteria("John.Smith", "pw", criteria);

        assertThat(result).isEmpty();
    }
}
