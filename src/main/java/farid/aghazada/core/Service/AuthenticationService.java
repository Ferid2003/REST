package farid.aghazada.core.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import farid.aghazada.core.Repository.TraineeRepository;
import farid.aghazada.core.Repository.TrainerRepository;

@Service
public class AuthenticationService {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    private HelperService helperService;

    @Autowired
    public void setHelperService(HelperService helperService) {
        this.helperService = helperService;
    }

    public boolean authenticateTrainee(String username, String password) {
        if(helperService.authenticate(username, password)) {
            return traineeRepository.findByUserUsername(username).isPresent();
        }
        return false;
    }

    public boolean authenticateTrainer(String username, String password) {
        if(helperService.authenticate(username, password)) {
            return trainerRepository.findByUserUsername(username).isPresent();
        }
        return false;
    }




}
