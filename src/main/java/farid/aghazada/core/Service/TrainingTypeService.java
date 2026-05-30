package farid.aghazada.core.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import farid.aghazada.core.Aspect.Annotation.RequiresAuth;
import farid.aghazada.core.Entity.TrainingType;
import farid.aghazada.core.Repository.TrainingTypeRepository;

@Service
public class TrainingTypeService {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;


    @RequiresAuth
    public List<TrainingType> getAllTrainingTypes(String username, String password) {
        return trainingTypeRepository.findAll();
    }
}
