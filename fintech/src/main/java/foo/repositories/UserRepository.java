package foo.repositories;

import foo.models.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<CustomUser, Long> {
    Optional<CustomUser> findByLogin(String login);
}
