package foo.dao;

import foo.exceptions.CreateUserException;
import foo.exceptions.IncorrectRoleException;
import foo.models.CustomUser;
import foo.models.Role;
import foo.repositories.RoleRepository;
import foo.repositories.UserRepository;
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
    public Optional<CustomUser> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long save(CustomUser customUser) {
        Optional<CustomUser> savedUser = userRepository.findByLogin(customUser.getLogin());
        if (savedUser.isPresent()) {
            throw new CreateUserException();
        }

        Role role = roleRepository.findByName(customUser.getRole().getName()).orElseThrow(IncorrectRoleException::new);
        customUser.setRole(role);

        userRepository.save(customUser);

        return customUser.getId();
    }
}
