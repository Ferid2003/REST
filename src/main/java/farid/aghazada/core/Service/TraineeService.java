package farid.aghazada.core.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import farid.aghazada.core.Aspect.Annotation.RequiresAuth;
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
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.TraineeRepository;
import farid.aghazada.core.Repository.TrainerRepository;
import farid.aghazada.core.Repository.TrainingRepository;

@Service
public class TraineeService {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    private HelperService helperService;

    @Autowired
    public void setHelperService(HelperService helperService) {
        this.helperService = helperService;
    }

    @Transactional
    public RegistrationResponseDto createTrainee(TraineeRegistrationDto dto) {
        String username = helperService.generateUsername(dto.firstName(), dto.lastName());
        String password = helperService.generatePassword();
        Trainee trainee = TraineeRegistrationDto.toTrainee(dto, username, password);
        traineeRepository.save(trainee);
        return RegistrationResponseDto.toRegistrationResponseDto(username, password);
    }

    @RequiresAuth
    public TraineeProfileResponseDto getTraineeByUsername(String username, String password) {
        return TraineeProfileResponseDto.toTrainerProfileResponseDto(findByUsernameOrThrow(username));
    }

    @RequiresAuth
    @Transactional
    public TraineeUpdateProfileResponseDto updateTrainee(String username, String password, TraineeUpdateDto dto) {
        Trainee trainee = findByUsernameOrThrow(username);

        if (dto.username() != null && !dto.username().isBlank()) {
            trainee.getUser().setUsername(dto.username());
        }
        if (dto.firstName() != null && !dto.firstName().isBlank()) {
            trainee.getUser().setFirstName(dto.firstName());
        }
        if (dto.lastName() != null && !dto.lastName().isBlank()) {
            trainee.getUser().setLastName(dto.lastName());
        }
        if (dto.dateOfBirth() != null) {
            trainee.setDateOfBirth(dto.dateOfBirth());
        }
        if (dto.address() != null) {
            trainee.setAddress(dto.address());
        }
        if (dto.isActive() != null) {
            trainee.getUser().setActive(dto.isActive());
        }

        return TraineeUpdateProfileResponseDto.toTraineeUpdateProfileReponseDto(trainee);
    }

    @RequiresAuth
    @Transactional
    public void changePassword(String username, String password, PasswordChangeDto dto) {
        Trainee trainee = findByUsernameOrThrow(username);
        if (!trainee.getUser().getPassword().equals(dto.oldPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }
        trainee.getUser().setPassword(dto.newPassword());
    }

    @RequiresAuth
    @Transactional
    public void changeIsActive(String username, String password, boolean isActive) {
        Trainee trainee = findByUsernameOrThrow(username);
        if(trainee.getUser().isActive() != isActive) {
            trainee.getUser().setActive(isActive);
        }else {
            throw new IllegalStateException("Trainee is already " + (isActive ? "active" : "inactive"));
        }
    }

    @RequiresAuth
    @Transactional
    public void deleteTraineeByUsername(String username, String password) {
        Trainee trainee = findByUsernameOrThrow(username);
        traineeRepository.delete(trainee);
    }

    @RequiresAuth
    public List<TrainingTraineeResponseDto> getTrainingsByCriteria(String username, String password, TrainingTraineeCriteriaDto criteria) {
        LocalDate fromDate = (criteria == null || criteria.fromDate() == null) ? null : criteria.fromDate();
        LocalDate toDate = (criteria == null || criteria.toDate() == null) ? null : criteria.toDate();
        String trainerName = (criteria == null || criteria.trainerName() == null) ? null : criteria.trainerName();
        String trainingTypeName = (criteria == null || criteria.trainingType() == null) ? null : criteria.trainingType().getName();
        return trainingRepository.findTraineeTrainingByCriteria(username, fromDate, toDate, trainerName, trainingTypeName).stream()
                .map(TrainingTraineeResponseDto::toTrainingResponseDto)
                .toList();
    }

    @RequiresAuth
    public List<TrainerSummaryDto> getUnassignedTrainers(String username, String password) {
        List<TrainerSummaryDto> unassignedTrainerDtos = new ArrayList<>();

        List<Trainer> trainers = trainerRepository.findTrainersNotAssignedToTrainee(username);

        for(Trainer trainer : trainers) {
            TrainerSummaryDto dto = TrainerSummaryDto.toTrainerSummaryDto(trainer);
            unassignedTrainerDtos.add(dto);
        }

        return unassignedTrainerDtos;
    }

    @RequiresAuth
    @Transactional
    public List<TrainerSummaryDto> updateTrainerList(String username, String password, TraineeTrainerUpdateDto dto) {
        Trainee trainee = findByUsernameOrThrow(username);

        Set<Trainer> newTrainerSet = new HashSet<>();
        List<String> usernames = dto.trainerUsernames();
        if (usernames != null && !usernames.isEmpty()) {
            List<Trainer> trainers = trainerRepository.findByUserUsernameIn(usernames);
            newTrainerSet.addAll(trainers);
        }

        trainee.setTrainers(newTrainerSet);

        return newTrainerSet.stream()
                .map(TrainerSummaryDto::toTrainerSummaryDto)
                .toList();
    }

    @RequiresAuth
    public boolean authenticate(String username, String password) {
        return traineeRepository.findByUserUsername(username).isPresent();
    }

    private Trainee findByUsernameOrThrow(String username) {
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Trainee with username " + username + " not found"));
    }

}
