package foo.other;

import foo.models.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class RoleMapper {
    public Optional<Role> getRole(ResultSet resultSet) throws SQLException {
        Optional<Role> role = Optional.empty();
        if (resultSet.next()){
            role = Optional.of(new Role(resultSet.getInt("id"),
                    resultSet.getString("name")));
        }

        resultSet.close();
        return role;

    }
}
