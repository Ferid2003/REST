package farid.aghazada.core.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import farid.aghazada.core.Aspect.Annotation.RequiresAuth;
import farid.aghazada.core.DTO.PasswordChangeDto;
import farid.aghazada.core.DTO.RegistrationResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerProfileResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerRegistrationDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateProfileResponseDto;
import farid.aghazada.core.DTO.Training.TrainingTrainerCriteriaDto;
import farid.aghazada.core.DTO.Training.TrainingTrainerResponseDto;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.TrainingType;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.TrainerRepository;
import farid.aghazada.core.Repository.TrainingRepository;
import farid.aghazada.core.Repository.TrainingTypeRepository;

@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    private HelperService helperService;

    @Autowired
    public void setHelperService(HelperService helperService) {
        this.helperService = helperService;
    }

    @Transactional
    public RegistrationResponseDto createTrainer(TrainerRegistrationDto dto) {
        String username = helperService.generateUsername(dto.firstName(), dto.lastName());
        String password = helperService.generatePassword();
        Trainer trainer = TrainerRegistrationDto.toTrainer(dto, username, password);

        if (dto.trainingType() != null) {
            TrainingType trainingType = trainingTypeRepository.findById(dto.trainingType().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + dto.trainingType().getId()));
            trainer.setSpecialization(trainingType);
        }

        trainerRepository.save(trainer);

        return RegistrationResponseDto.toRegistrationResponseDto(username, password);
    }

    @RequiresAuth
    public TrainerProfileResponseDto getTrainerByUsername(String username, String password) {
        return TrainerProfileResponseDto.toTrainerProfileResponseDto(findByUsernameOrThrow(username));
    }

    @RequiresAuth
    @Transactional
    public TrainerUpdateProfileResponseDto updateTrainer(String username, String password, TrainerUpdateDto dto) {
        Trainer trainer = findByUsernameOrThrow(username);

        if(dto.username() != null && !dto.username().isBlank()) {
            trainer.getUser().setUsername(dto.username());
        }
        if (dto.firstName() != null && !dto.firstName().isBlank()) {
            trainer.getUser().setFirstName(dto.firstName());
        }
        if (dto.lastName() != null && !dto.lastName().isBlank()) {
            trainer.getUser().setLastName(dto.lastName());
        }
        if (dto.specialization() != null) {
            TrainingType trainingType = trainingTypeRepository.findById(dto.specialization().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + dto.specialization().getId()));
            trainer.setSpecialization(trainingType);
        }
        if (dto.isActive() != null) {
            trainer.getUser().setActive(dto.isActive());
        }

        return TrainerUpdateProfileResponseDto.toTrainerUpdateProfileResponseDto(trainer);
    }

    @RequiresAuth
    @Transactional
    public void changePassword(String username, String password, PasswordChangeDto dto) {
        Trainer trainer = findByUsernameOrThrow(username);
        if (!trainer.getUser().getPassword().equals(dto.oldPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }
        trainer.getUser().setPassword(dto.newPassword());
    }

    @RequiresAuth
    @Transactional
    public void changeIsActive(String username, String password, boolean isActive) {
        Trainer trainer = findByUsernameOrThrow(username);
        if(trainer.getUser().isActive() != isActive) {
            trainer.getUser().setActive(isActive);
        }else {
            throw new IllegalStateException("Trainer is already " + (isActive ? "active" : "inactive"));
        }
    }

    @RequiresAuth
    public List<TrainingTrainerResponseDto> getTrainingsByCriteria(String username, String password, TrainingTrainerCriteriaDto criteria) {
        LocalDate fromDate = criteria == null ? null : criteria.fromDate();
        LocalDate toDate = criteria == null ? null : criteria.toDate();
        String traineeName = criteria == null ? null : criteria.traineeName();
        return trainingRepository.findTrainerTrainingByCriteria(username, fromDate, toDate, traineeName).stream()
                .map(TrainingTrainerResponseDto::toTrainingResponseDto)
                .toList();
    }

    @RequiresAuth
    public boolean authenticate(String username, String password) {
        return trainerRepository.findByUserUsername(username).isPresent();
    }

    private Trainer findByUsernameOrThrow(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Trainer with username " + username + " not found"));
    }
}
