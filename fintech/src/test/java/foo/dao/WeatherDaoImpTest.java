package foo.dao;

import foo.exceptions.InvalidWeatherType;
import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIf(expression = "#{'${weather-dao-realization}' != 'in-memory'}", loadContext = true)
@ActiveProfiles("test")
@Testcontainers
class WeatherDaoImpTest {

    @Autowired
    WeatherDao weatherDao;

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
        connection.prepareStatement("TRUNCATE TABLE weathers").execute();
        connection.close();
    }


    @Test
    void saveNewRegionWithWeatherWithSomeThreads() throws SQLException {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<Weather> weatherRequests = List.of(
                Weather.builder()
                        .temperature(21.1)
                        .city(new City("Test1"))
                        .weatherType(new WeatherType("sunshine"))
                        .date(dateTime)
                        .build(),
                Weather.builder()
                        .temperature(12.1)
                        .city(new City("Test2"))
                        .weatherType(new WeatherType("raining"))
                        .date(dateTime)
                        .build(),
                Weather.builder()
                        .temperature(-13.1)
                        .city(new City("Test3"))
                        .weatherType(new WeatherType("snowing"))
                        .date(dateTime)
                        .build()
        );

        List<Thread> threads = new ArrayList<>();

        for (Weather weather : weatherRequests) {
            threads.add(new Thread(() -> weatherDao.saveWeatherWithNewRegion(weather)));
        }

        for(Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Connection connection = dataSource.getConnection();
        String sql = "SELECT * FROM weathers JOIN cities ON weathers.city_id = cities.id JOIN weather_types ON weathers.type_id = weather_types.id";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        int i = 0;

        while (resultSet.next()) {
            i++;
            Weather actualWeather = new Weather(resultSet.getLong("weathers.id"),
                    new City(resultSet.getLong("cities.id"), resultSet.getString("cities.name")),
                    new WeatherType(resultSet.getLong("weather_types.id"), resultSet.getString("weather_types.type")),
                    resultSet.getDouble("weathers.temperature"),
                    resultSet.getObject("weathers.date_time", LocalDateTime.class));
            assertThat(weatherRequests).contains(actualWeather);
        }

        assertThat(i).isEqualTo(3);
    }

    @Test
    void FailedSaveNewRegion() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weatherRequests =
                Weather.builder()
                        .temperature(-13.1)
                        .city(new City("Test3"))
                        .weatherType(new WeatherType("1111"))
                        .date(dateTime)
                        .build();

        assertThrows(InvalidWeatherType.class, () -> weatherDao.saveWeatherWithNewRegion(weatherRequests));

    }


    @Test
    void saveWeatherAndType() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            List<Weather> weatherRequests = List.of(
                    Weather.builder()
                            .temperature(21.1)
                            .city(new City("Test1"))
                            .weatherType(new WeatherType("test1"))
                            .date(dateTime)
                            .build(),
                    Weather.builder()
                            .temperature(12.1)
                            .city(new City("Test2"))
                            .weatherType(new WeatherType("test2"))
                            .date(dateTime)
                            .build(),
                    Weather.builder()
                            .temperature(-13.1)
                            .city(new City("Test3"))
                            .weatherType(new WeatherType("test3"))
                            .date(dateTime)
                            .build()
            );

            String foundType = "SELECT id FROM weather_types WHERE type = ?";
            PreparedStatement notFoundTypeStatement = connection.prepareStatement(foundType);
            notFoundTypeStatement.setString(1, "test");
            ResultSet resultNotFound = notFoundTypeStatement.executeQuery();
            assertThat(resultNotFound.next()).isFalse();
            resultNotFound.close();
            notFoundTypeStatement.close();

            List<Thread> threads = new ArrayList<>();

            for (Weather weather : weatherRequests) {
                threads.add(new Thread(() -> weatherDao.saveWeatherAndType(weather)));
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            String sql = "SELECT * FROM weathers JOIN cities ON weathers.city_id = cities.id JOIN weather_types ON weathers.type_id = weather_types.id";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;

            while (resultSet.next()) {
                i++;
                Weather actualWeather = new Weather(resultSet.getLong("weathers.id"),
                        new City(resultSet.getLong("cities.id"), resultSet.getString("cities.name")),
                        new WeatherType(resultSet.getLong("weather_types.id"), resultSet.getString("weather_types.type")),
                        resultSet.getDouble("weathers.temperature"),
                        resultSet.getObject("weathers.date_time", LocalDateTime.class));
                assertThat(weatherRequests).contains(actualWeather);
            }

            PreparedStatement foundTypeStatement = connection.prepareStatement(foundType);
            foundTypeStatement.setString(1, "test1");
            ResultSet resultFound = foundTypeStatement.executeQuery();
            assertThat(resultFound.next()).isTrue();
            assertThat(resultFound.next()).isFalse();
            resultFound.close();
            foundTypeStatement.close();

            assertThat(i).isEqualTo(3);
        }

    }

    @Test
    void findWeatherWhenNotExists() {
        Optional<Weather> actual = weatherDao.findByRegionId(333L, LocalDateTime.now());

        assertThat(actual).isEmpty();
    }

    @Test
    void findWeatherByRegionWhenExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Long id = weatherDao.saveWeatherWithNewRegion(Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build());

        Optional<Weather> actual = weatherDao.findByRegionName("Test", dateTime);

        assertThat(actual).isPresent();
        assertThat(actual.get().getCity().getId()).isEqualTo(id);
    }

    @Test
    void findWeatherWhenExistsButHasAnotherDateTime() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Long id = weatherDao.saveWeatherWithNewRegion(Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build());
        Optional<Weather> actual = weatherDao.findByRegionId(id, dateTime.plusMinutes(1L));

        assertThat(actual).isEmpty();
    }


    @Test
    void updateWeatherByRegionWhenExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build();

        Long id = weatherDao.saveWeatherWithNewRegion(weather);

        Weather willBeUpdated = Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("raining"))
                .date(dateTime)
                .temperature(5.0)
                .build();



        Long result = weatherDao.updateByRegionNameAndCreateIfNotExists(willBeUpdated);
        Optional<Weather> actual  = weatherDao.findByRegionId(id, dateTime);

        assertThat(result).isEqualTo(-1);
        assertThat(actual).isPresent();
        assertThat(actual.get().getTemperature()).isEqualTo(5.0);
    }

    @Test
    void updateWeatherByRegionWhenRegionNotExists() {
        LocalDateTime dateTime = LocalDateTime.now();
        Weather willBeCreated = Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build();

        Long result = weatherDao.updateByRegionNameAndCreateIfNotExists(willBeCreated);


        assertThat(result).isNotNegative().isEqualTo(willBeCreated.getCity().getId());
    }

    @Test
    void updateWeatherByRegionWhenDateNotExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        Weather weather = Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build();
        weatherDao.saveWeatherWithNewRegion(weather);

        Weather willBeCreated = Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime.plusMinutes(1))
                .temperature(13.2)
                .build();

        Long actual = weatherDao.updateByRegionNameAndCreateIfNotExists(willBeCreated);

            assertThat(actual).isEqualTo(willBeCreated.getCity().getId()).isEqualTo(weather.getCity().getId());
    }

    @Test
    void deleteByRegionName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

            String regionName = "Will be created";
            Weather weather1 = Weather.builder()
                    .city(new City(regionName))
                    .weatherType(new WeatherType("sunshine"))
                    .date(dateTime)
                    .temperature(13.2)
                    .build();

            Weather weather2 = Weather.builder()
                    .city(new City(regionName))
                    .weatherType(new WeatherType("sunshine"))
                    .date(dateTime.plusMinutes(1))
                    .temperature(13.2)
                    .build();
            Long citiesId = weatherDao.saveWeatherWithNewRegion(weather1);
            weatherDao.saveWeatherWithNewRegion(weather2);

            weatherDao.deleteByRegionName(weather1.getCity().getName());
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(id) FROM weathers WHERE city_id = ?");
            preparedStatement.setLong(1, citiesId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int countRow = resultSet.getInt(1);

            assertThat(countRow).isZero();
        }
    }

    @Test
    void deleteByRegionId() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

            String regionName = "Will be created";
            Weather weather1 = Weather.builder()
                    .city(new City(regionName))
                    .weatherType(new WeatherType("sunshine"))
                    .date(dateTime)
                    .temperature(13.2)
                    .build();

            Long regionId = weatherDao.saveWeatherWithNewRegion(weather1);

            weatherDao.deleteByRegionId(regionId);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(id) FROM weathers WHERE city_id = ?");
            preparedStatement.setLong(1, regionId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int countRow = resultSet.getInt(1);

            assertThat(countRow).isZero();
        }
    }

}