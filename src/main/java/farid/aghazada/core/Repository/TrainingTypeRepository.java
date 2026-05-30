package farid.aghazada.core.Repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import farid.aghazada.core.Entity.TrainingType;

@Repository
public interface TrainingTypeRepository extends CrudRepository<TrainingType, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<TrainingType> findAll();

}
