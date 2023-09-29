package foo.fintech.services;

import foo.fintech.models.WeatherRequest;
import foo.fintech.models.Weather;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WeatherServiceTest {
    @Autowired
    WeatherService weatherService;

    @Test
    @Order(1)
    void addNewRegionWithWeatherWithSomeThreads() {
        LocalDateTime dateTime = LocalDateTime.now();
        List<WeatherRequest> weatherRequests = List.of(
            new WeatherRequest(11.1, dateTime),
            new WeatherRequest(12.1, dateTime),
            new WeatherRequest(-13.1, dateTime)
        );

        List<Long> actual = new ArrayList<>();
        for (WeatherRequest weatherRequest : weatherRequests) {
            new Thread(() -> actual.add(weatherService.addNewRegionWithWeather("Test", weatherRequest))).start();
        }

        assertThat(actual).containsExactlyInAnyOrder(0L, 1L, 2L);
    }

    @Test
    @Order(2)
    void findWeatherWhenNotExists() {
        Optional<Weather> actual = weatherService.findWeatherByRegion("Not Exists", LocalDateTime.now(), Weather::getRegionId);

        assertThat(actual).isEmpty();
    }

    @Test
    @Order(3)
    void findWeatherByRegionWhenExists() {
        LocalDateTime dateTime = LocalDateTime.now();
        Long id = weatherService.addNewRegionWithWeather("For find", new WeatherRequest(0.1, dateTime));
        Optional<Weather> actual = weatherService.findWeatherByRegion("For find", dateTime, Weather::getRegionName);

        assertThat(actual).isPresent();
        assertThat(actual.get().getRegionId()).isEqualTo(id);
    }

    @Test
    @Order(4)
    void findWeatherWhenExistsButHasAnotherDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        Long id = weatherService.addNewRegionWithWeather("Fail to find", new WeatherRequest(1.1, dateTime));
        Optional<Weather> actual = weatherService.findWeatherByRegion("For fail to find", dateTime.plusMinutes(1L), Weather::getRegionId);

        assertThat(actual).isEmpty();
    }


    @Test
    @Order(5)
    void updateWeatherByRegionWhenExists() {
        LocalDateTime dateTime = LocalDateTime.now();
        Long id = weatherService.addNewRegionWithWeather("Will be updated", new WeatherRequest(1.1, dateTime));

        Long result = weatherService.updateWeatherByRegion("Will be updated", new WeatherRequest(5.0, dateTime));
        Optional<Weather> actual  = weatherService.findWeatherByRegion(id , dateTime, Weather::getRegionId);

        assertThat(result).isEqualTo(-1);
        assertThat(actual).isPresent();
        assertThat(actual.get().getTemperature()).isEqualTo(5.0);
    }

    @Test
    @Order(6)
    void updateWeatherByRegionWhenRegionNotExists() {
        LocalDateTime dateTime = LocalDateTime.now();

        Long actual = weatherService.updateWeatherByRegion("Will be created", new WeatherRequest(5.0, dateTime));
        Optional<Weather> result = weatherService.findWeatherByRegion("Will be created", dateTime, Weather::getRegionName);


        assertThat(actual).isNotNegative();
        assertThat(result).isPresent();
        assertThat(result.get().getRegionId()).isEqualTo(actual);
    }

    @Test
    @Order(7)
    void updateWeatherByRegionWheDateNotExists() {
        LocalDateTime dateTime = LocalDateTime.now();
        Long regionId = weatherService.addNewRegionWithWeather( "Create for update", new WeatherRequest(5.0, dateTime));
        dateTime = dateTime.plusMinutes(1L);

        Long actual = weatherService.updateWeatherByRegion("Create for update", new WeatherRequest(5.0, dateTime));
        Optional<Weather> result = weatherService.findWeatherByRegion("Create for update", dateTime, Weather::getRegionName);


        assertThat(actual).isEqualTo(regionId);
        assertThat(result).isPresent();
        assertThat(result.get().getRegionId()).isEqualTo(actual);
    }


    @Test
    @Order(8)
    void removeWeathersByParameter() {
        LocalDateTime dateTime = LocalDateTime.now();
        Long regionId = weatherService.addNewRegionWithWeather( "Created for delete", new WeatherRequest(4.1, dateTime));
        Long otherRegionId = weatherService.addNewRegionWithWeather("Created for delete", new WeatherRequest(4.1, dateTime));

        Boolean actual  = weatherService.removeWeathersByParameter("Created for delete", Weather::getRegionName);

        assertThat(actual).isTrue();
        assertThat(weatherService.findWeatherByRegion("Created for delete", dateTime, Weather::getRegionName)).isEmpty();

    }
}