package farid.aghazada.core.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import farid.aghazada.core.Entity.Training;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, TrainingRepositoryCustom {

    Optional<Training> findByTrainingName(String trainingName);

}
