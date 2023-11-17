package foo.dao;

import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;
import foo.other.WeatherCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIf(expression = "#{'${weather-dao-realization}' == 'jpa'}", loadContext = true)
@ActiveProfiles("test")
@Testcontainers
public class WeatherDaoWithCacheTest {
    @Autowired
    WeatherDaoJpa weatherDao;

    @Autowired
    DataSource dataSource;


    @SpyBean
    WeatherCache weatherCache;

    @DynamicPropertySource
    static void setPropertySource(DynamicPropertyRegistry dynamicPropertySource) {
        dynamicPropertySource.add("spring.datasource.url",
                () -> String.format("jdbc:h2:tcp://%s:%d/test", h2Container.getHost(), h2Container.getMappedPort(1521)));
    }

    @Container
    public static GenericContainer<?> h2Container =
            new GenericContainer<>(DockerImageName.parse("oscarfonts/h2"))
                    .withExposedPorts(1521).withEnv("H2_OPTIONS", "-ifNotExists");

    static {
        h2Container.start();
    }

    @Test
    void getWeatherWhenNotContainsInCache() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                .city(new City("Test1"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build();
        Long id = weatherDao.saveWeatherAndType(weather);

        Optional<Weather> actual = weatherDao.findByRegionName("Test1", dateTime);

        assertThat(actual).isPresent();
        assertThat(actual.get().getCity().getId()).isEqualTo(id);
        verify(weatherCache, times(1)).addWeatherToCache(any(), any());
    }

    @Test
    void getWeatherWhenContainsInCache() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                .city(new City("Test2"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build();
        Long id = weatherDao.saveWeatherWithNewRegion(weather);

        weatherDao.findByRegionName("Test2", dateTime);
        Optional<Weather> actual = weatherDao.findByRegionName("Test2", dateTime);

        assertThat(actual).isPresent();
        assertThat(actual.get().getCity().getId()).isEqualTo(id);
        verify(weatherCache, times(2)).getWeatherFromCache(any());
        verify(weatherCache, times(1)).addWeatherToCache(any(), any());
    }

    @Test
    void updateWeatherInCache() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                .city(new City("Test3"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build();

        weatherDao.saveWeatherWithNewRegion(weather);
        weatherDao.findByRegionName("Test3", dateTime);
        weatherDao.updateByRegionNameAndCreateIfNotExists(weather);
        weatherDao.findByRegionName("Test3", dateTime);

        verify(weatherCache, times(2)).addWeatherToCache(any(), any());
        verify(weatherCache, times(1)).removeFromCache(any());
    }

}
