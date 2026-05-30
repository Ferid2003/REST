package farid.aghazada.core.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import farid.aghazada.core.Entity.Trainer;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserUsername(String username);

    List<Trainer> findByUserUsernameIn(List<String> usernames);

    @Query("""
           SELECT tr
           FROM Trainer tr
           WHERE tr.id NOT IN (
               SELECT assigned.id
               FROM Trainee t
               JOIN t.trainers assigned
               WHERE t.user.username = :traineeUsername
           )
           """)
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);
}
