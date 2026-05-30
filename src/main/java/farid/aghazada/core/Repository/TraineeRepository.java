package farid.aghazada.core.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import farid.aghazada.core.Entity.Trainee;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

    List<Trainee> findByUserUsernameIn(List<String> usernames);

}
