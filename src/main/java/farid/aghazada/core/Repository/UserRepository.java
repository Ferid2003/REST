package farid.aghazada.core.Repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

import farid.aghazada.core.Entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

    @Query("""
            SELECT u.username
            FROM User u
            WHERE u.username = :baseUsername
               OR u.username LIKE CONCAT(:baseUsername, '%')
            """)
    List<String> findAllUsernamesForBase(@Param("baseUsername") String baseUsername);

    Boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
