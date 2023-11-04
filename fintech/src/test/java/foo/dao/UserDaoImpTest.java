package foo.dao;

import foo.exceptions.CreateUserException;
import foo.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class UserDaoImpTest {

    @Autowired
    UserDao userDao;

    @Autowired
    DataSource dataSource;

    @Container
    public static GenericContainer<?> h2Container =
            new GenericContainer<>(DockerImageName.parse("oscarfonts/h2"))
                    .withExposedPorts(1521).withEnv("H2_OPTIONS", "-ifNotExists");

    static {
        h2Container.start();
    }

    @DynamicPropertySource
    static void setPropertySource(DynamicPropertyRegistry dynamicPropertySource) {
        dynamicPropertySource.add("spring.datasource.url",
                () -> String.format("jdbc:h2:tcp://%s:%d/test", h2Container.getHost(), h2Container.getMappedPort(1521)));
    }


    @BeforeEach
    public  void deleteWeathers() throws SQLException{
        Connection connection = dataSource.getConnection();
        connection.prepareStatement("TRUNCATE TABLE users").execute();
        connection.close();
    }


    @Test
    void saveNewUser() throws SQLException {
        CustomUser customUser = CustomUser.builder()
                .username("test")
                .role(new Role("ROLE_USER"))
                .password("test")
                .build();

        userDao.save(customUser);

        Connection connection = dataSource.getConnection();
        String sql = "SELECT users.id, users.username, users.password, roles.id, roles.name " +
                "FROM users JOIN roles ON users.role_id = roles.id WHERE users.username='test'";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        assertThat(resultSet.next()).isTrue();

        CustomUser actual = new CustomUser(resultSet.getLong("users.id"),
            resultSet.getString("users.username"),
            resultSet.getString("users.password"),
            new Role(resultSet.getInt("roles.id"), resultSet.getString("roles.name")));

        assertThat(actual).isEqualTo(customUser);

    }

    @Test
    void FailedSaveNewRegion() {
        CustomUser customUser1 = CustomUser.builder()
                .username("test")
                .role(new Role("ROLE_USER"))
                .password("test")
                .build();

        CustomUser customUser2 = CustomUser.builder()
                .username("test")
                .role(new Role("ROLE_USER"))
                .password("test")
                .build();

        userDao.save(customUser1);
        assertThrows(CreateUserException.class, () -> userDao.save(customUser2));

    }

    @Test
    void findWeatherWhenNotExists() {
        Optional<CustomUser> actual = userDao.findByUsername("test");

        assertThat(actual).isEmpty();
    }

    @Test
    void findWeatherByRegionWhenExists() {
        CustomUser customUser = CustomUser.builder()
                .username("test")
                .role(new Role("ROLE_USER"))
                .password("test")
                .build();

        userDao.save(customUser);

        Optional<CustomUser> actual = userDao.findByUsername("test");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUsername()).isEqualTo("test");
        assertThat(actual.get().getRole().getName()).isEqualTo("ROLE_USER");
    }

}