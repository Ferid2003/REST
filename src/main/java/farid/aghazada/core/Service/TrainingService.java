package farid.aghazada.core.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import farid.aghazada.core.Aspect.Annotation.RequiresAuth;
import farid.aghazada.core.DTO.Training.TrainingCreationDto;
import farid.aghazada.core.Entity.Trainee;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.Training;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.TraineeRepository;
import farid.aghazada.core.Repository.TrainerRepository;
import farid.aghazada.core.Repository.TrainingRepository;

@Service
public class TrainingService {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @RequiresAuth
    @Transactional
    public void createTraining(String username, String password, TrainingCreationDto dto) {
        Trainer trainer = trainerRepository.findByUserUsername(dto.trainerUsername())
                .orElseThrow(() -> new UserNotFoundException("Trainer with username " + dto.trainerUsername() + " not found"));

        Trainee trainee = traineeRepository.findByUserUsername(dto.traineeUsername())
                .orElseThrow(() -> new UserNotFoundException("Trainee with username " + dto.traineeUsername() + " not found"));
            
        Training training = TrainingCreationDto.toTraining(dto, trainer, trainee);

        trainingRepository.save(training);
    }

}
