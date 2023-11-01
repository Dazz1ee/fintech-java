package foo.dao;

import foo.models.CustomUser;

import java.util.Optional;

public interface UserDao {
    Optional<CustomUser> findByLogin(String login);
    Long save(CustomUser customUser);
}
