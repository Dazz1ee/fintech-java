package foo.dao;

import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherDaoInMemoryTest {

    WeatherDao weatherDao;

    @BeforeAll
    public void init() {
        weatherDao = new WeatherDaoImp();
    }

    @Test
    @Order(1)
    void saveNewRegionWithWeatherWithSomeThreads() {
        LocalDateTime dateTime = LocalDateTime.now();
        List<Weather> weatherRequests = List.of(
                Weather.builder()
                        .temperature(11.1)
                        .weatherType(new WeatherType("sunshine"))
                        .city(new City("Test1"))
                        .date(dateTime)
                        .build(),
                Weather.builder()
                        .temperature(12.1)
                        .weatherType(new WeatherType("sunshine"))
                        .city(new City("Test2"))
                        .date(dateTime)
                        .build(),
                Weather.builder()
                        .temperature(-13.1)
                        .weatherType(new WeatherType("sunshine"))
                        .city(new City("Test3"))
                        .date(dateTime)
                        .build()
        );

        List<Long> actual = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (Weather weather : weatherRequests) {
            threads.add(new Thread(() -> actual.add(weatherDao.saveWeatherWithNewRegion(weather))));
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

        assertThat(actual).containsExactlyInAnyOrder(0L, 1L, 2L);
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
        LocalDateTime dateTime = LocalDateTime.now();
        Long id = weatherDao.saveWeatherWithNewRegion(Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City("For find"))
                .date(dateTime)
                .temperature(13.2)
                .build());

        Optional<Weather> actual = weatherDao.findByRegionName("For find", dateTime);

        assertThat(actual).isPresent();
        assertThat(actual.get().getCity().getId()).isEqualTo(id);
    }

    @Test
    @Order(4)
    void findWeatherWhenExistsButHasAnotherDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        Long id = weatherDao.saveWeatherWithNewRegion(Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City("Test"))
                .date(dateTime)
                .temperature(13.2)
                .build());
        Optional<Weather> actual = weatherDao.findByRegionId(id, dateTime.plusMinutes(1L));

        assertThat(actual).isEmpty();
    }


    @Test
    @Order(5)
    void updateWeatherByRegionWhenExists() {
        LocalDateTime dateTime = LocalDateTime.now();
        Weather weather = Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City("Will be updated"))
                .date(dateTime)
                .temperature(13.2)
                .build();

        Long id = weatherDao.saveWeatherWithNewRegion(weather);

        Weather willBeUpdated = Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City("Will be updated"))
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
                .weatherType(new WeatherType("sunshine"))
                .city(new City("Will be created"))
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
        LocalDateTime dateTime = LocalDateTime.now();
        Weather weather = Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City("Create for update"))
                .date(dateTime)
                .temperature(13.2)
                .build();
        weatherDao.saveWeatherWithNewRegion(weather);

        Weather willBeCreated = Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City("Create for update"))
                .date(dateTime.plusMinutes(1))
                .temperature(13.2)
                .build();

        Long actual = weatherDao.updateByRegionNameAndCreateIfNotExists(willBeCreated);

        assertThat(actual).isEqualTo(willBeCreated.getCity().getId()).isEqualTo(weather.getCity().getId());
    }

    @Test
    @Order(8)
    void deleteByRegionName() {
        LocalDateTime dateTime = LocalDateTime.now();
        String regionName = "Will be created";
        Weather weather1 = Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City(regionName))
                .date(dateTime)
                .temperature(13.2)
                .build();

        Weather weather2 = Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City(regionName))
                .date(dateTime)
                .temperature(13.2)
                .build();

        weatherDao.saveWeatherWithNewRegion(weather1);
        weatherDao.saveWeatherWithNewRegion(weather2);

        Boolean actual  = weatherDao.deleteByRegionName(weather1.getCity().getName());

        assertThat(actual).isTrue();
        assertThat(weatherDao.findByRegionName(regionName, dateTime)).isEmpty();
    }

    @Test
    @Order(9)
    void deleteByRegionId() {
        LocalDateTime dateTime = LocalDateTime.now();
        String regionName = "Will be created";
        Weather weather1 = Weather.builder()
                .weatherType(new WeatherType("sunshine"))
                .city(new City(regionName))
                .date(dateTime)
                .temperature(13.2)
                .build();


        Long regionId = weatherDao.saveWeatherWithNewRegion(weather1);

        Boolean actual  = weatherDao.deleteByRegionId(regionId);

        assertThat(actual).isTrue();
        assertThat(weatherDao.findByRegionId(regionId, dateTime)).isEmpty();
    }

}