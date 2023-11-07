package foo.dao;

import foo.exceptions.CreateUserException;
import foo.exceptions.IncorrectRoleException;
import foo.models.CustomUser;
import foo.models.Role;
import foo.repositories.RoleRepository;
import foo.repositories.UserRepository;
import jakarta.validation.ConstraintDeclarationException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
public class UserDaoJpa implements UserDao{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<CustomUser> findByUsername(String login) {
        return userRepository.findByUsername(login);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long save(CustomUser customUser) {
        Role role = roleRepository.findByName(customUser.getRole().getName()).orElseThrow(IncorrectRoleException::new);
        customUser.setRole(role);

        try {
            userRepository.save(customUser);
        } catch (ConstraintDeclarationException exception) {
            throw new CreateUserException(exception);
        } catch (Exception exception) {
            throw new CreateUserException("Unknown execption", exception);
        }

        return customUser.getId();
    }
}
