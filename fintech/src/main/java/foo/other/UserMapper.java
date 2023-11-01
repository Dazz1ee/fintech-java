package foo.other;

import foo.models.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserMapper {
    public Optional<CustomUser> getUser(ResultSet resultSet) throws SQLException {
        Optional<CustomUser> user = Optional.empty();
        if (resultSet.next()){
            user = Optional.of(new CustomUser(resultSet.getLong("user_.id"),
                    resultSet.getString("user_.username"),
                    resultSet.getString("user_.login"),
                    resultSet.getString("user_.password"),
                    new Role(resultSet.getInt("role.id"), resultSet.getString("role.name"))));
        }

        resultSet.close();
        return user;

    }
    public Optional<Long> getId(ResultSet resultSet) throws SQLException {
        Optional<Long> id = Optional.empty();
        if (resultSet.next()){
            id = Optional.of(resultSet.getLong("id"));
        }

        resultSet.close();
        return id;

    }

}
