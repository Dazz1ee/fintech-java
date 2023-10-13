package foo.dao;

import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"api_key: aaa"})
class WeatherDaoImpTest {

    @Autowired
    WeatherDao weatherDao;

    @Autowired
    DataSource dataSource;

    @BeforeEach()
    public  void deleteWeathers() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.prepareStatement("TRUNCATE TABLE weather").execute();
        connection.close();
    }


    @Test
    @Order(1)
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
        String sql = "SELECT * FROM weather JOIN city ON weather.city_id = city.id JOIN weather_type ON weather.type_id = weather_type.id";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        int i = 0;

        while (resultSet.next()) {
            i++;
            Weather actualWeather = new Weather(resultSet.getLong("weather.id"),
                    new City(resultSet.getLong("city.id"), resultSet.getString("city.name")),
                    new WeatherType(resultSet.getLong("weather_type.id"), resultSet.getString("weather_type.type")),
                    resultSet.getDouble("weather.temperature"),
                    resultSet.getObject("weather.date_time", LocalDateTime.class));
            assertThat(weatherRequests).contains(actualWeather);
        }

        assertThat(i).isEqualTo(3);
    }

    @Test
    @Order(2)
    void findWeatherWhenNotExists() {
        Optional<Weather> actual = weatherDao.findByRegionId(333L, LocalDateTime.now());

        assertThat(actual).isEmpty();
    }

    @Test
    @Order(3)
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
    @Order(4)
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
    @Order(5)
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
    @Order(6)
    void updateWeatherByRegionWhenRegionNotExists() {
        LocalDateTime dateTime = LocalDateTime.now();
        Weather willBeCreated = Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build();

        Long result = weatherDao.updateByRegionNameAndCreateIfNotExists(willBeCreated);


        assertThat(result).isNotNegative();
        assertThat(result).isEqualTo(willBeCreated.getCity().getId());
    }

    @Test
    @Order(7)
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
    @Order(8)
    void deleteByRegionName() throws SQLException {
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
        Long cityId = weatherDao.saveWeatherWithNewRegion(weather1);
        weatherDao.saveWeatherWithNewRegion(weather2);

        weatherDao.deleteByRegionName(weather1.getCity().getName());
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(id) FROM weather WHERE city_id = ?");
        preparedStatement.setLong(1, cityId);

        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int countRow = resultSet.getInt(1);

        assertThat(countRow).isZero();
    }

    @Test
    @Order(9)
    void deleteByRegionId() throws SQLException {
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
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(id) FROM weather WHERE city_id = ?");
        preparedStatement.setLong(1, regionId);

        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int countRow = resultSet.getInt(1);

        assertThat(countRow).isZero();
    }

}