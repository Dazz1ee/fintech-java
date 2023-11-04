package foo.dao;

import foo.exceptions.*;
import foo.models.CustomUser;
import foo.other.RoleMapper;
import foo.other.UserMapper;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@RequiredArgsConstructor
public class UserDaoJdbc implements UserDao {

    private final DataSource dataSource;

    @Override
    public Optional<CustomUser> findByUsername(String username) {
        Connection connection = getConnection();
        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            String sql =
                    "SELECT users.id, users.username, users.password, roles.id, roles.name " +
                            "FROM users JOIN roles ON users.role_id = roles.id WHERE users.username=?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            UserMapper userMapper = new UserMapper();
            Optional<CustomUser> result = userMapper.getUser(resultSet);

            preparedStatement.close();
            connection.commit();
            return result;
        } catch (SQLException exception) {
            rollbackTransaction(connection, exception);
            throw new UnknownSQLException(exception);
        }
    }

    @Override
    public Long save(CustomUser customUser) {
        Connection connection = getConnection();

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);

            String sqlSelectRole = "SELECT id, name FROM roles WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectRole);
            preparedStatement.setString(1, customUser.getRole().getName());

            ResultSet resultSet = preparedStatement.executeQuery();
            RoleMapper rolesMapper = new RoleMapper();
            customUser.setRole(rolesMapper.getRole(resultSet).orElseThrow(() -> {
                try {
                    preparedStatement.close();
                    connection.rollback();
                } catch (SQLException e) {
                    throw new UnknownSQLException(e);
                }

                return new IncorrectRoleException();
            }));

            preparedStatement.close();

            String sqlSave = "INSERT INTO users(username, password, role_id) VALUES (?, ?, ?)";
            PreparedStatement userStatement = connection.prepareStatement(sqlSave, Statement.RETURN_GENERATED_KEYS);
            userStatement.setString(1, customUser.getUsername());
            userStatement.setString(2, customUser.getPassword());
            userStatement.setInt(3, customUser.getRole().getId());

            try {
                userStatement.executeUpdate();
            } catch (SQLException sqlException) {
                rollbackTransaction(connection, sqlException);
                throw new CreateUserException(sqlException);
            }

            ResultSet idResultSet = userStatement.getGeneratedKeys();
            UserMapper userMapper = new UserMapper();
            Long id = userMapper.getId(idResultSet).orElseThrow(() -> {
                rollbackTransaction(connection);

                try {
                    userStatement.close();
                } catch (SQLException e) {
                    throw new UnknownSQLException(e);
                }

                return new CreateUserException("unknown reason");
            });

            customUser.setId(id);
            userStatement.close();
            connection.commit();

            return id;
        } catch (SQLException exception) {
            rollbackTransaction(connection, exception);
            throw new UnknownSQLException(exception);
        }
    }
    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new GetConnectionException(e);
        }

    }

    private void rollbackTransaction(Connection connection, Exception exception) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RollbackException(e.initCause(exception));
        }
    }

    private void rollbackTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RollbackException(e);
        }
    }
}
