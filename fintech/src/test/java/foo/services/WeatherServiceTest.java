package foo.services;

import foo.configurations.UriBuilderConfig;
import foo.dao.WeatherDao;
import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherRequest;
import foo.models.WeatherType;
import foo.other.CustomUriBuilder;
import foo.other.BiLoadingLRUCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {UriBuilderConfig.class, WeatherService.class})
@EnableConfigurationProperties(value = UriBuilderConfig.class)
@ActiveProfiles("test")
class WeatherServiceTest {

    @Autowired
    WeatherService weatherService;

    @MockBean
    WeatherDao weatherDao;

    @MockBean
    BiLoadingLRUCache<Weather> biLoadingLRUCache;

    @Autowired
    @Qualifier("uriBuilderForWeatherApi")
    CustomUriBuilder customUriBuilder;


    @Test
    void addNewRegionWithWeatherWithSomeThreads() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Long regionId = 1L;
        WeatherRequest weatherRequest = new WeatherRequest(12.1, "sunshine", dateTime);
        Weather weather = Weather.builder()
                .date(dateTime)
                .temperature(12.1)
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .build();
        when(weatherDao.saveWeatherWithNewRegion(weather)).thenReturn(regionId);

        URI uri = weatherService.addNewRegionWithWeather("Test", weatherRequest);

        assertThat(customUriBuilder.getUri(regionId, dateTime)).isEqualTo(uri);

    }

    @Test
    void findWeatherWhenNotExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Long regionId = 1L;
        Optional<Double> actual = weatherService.findWeatherByRegion(regionId, dateTime);

        assertThat(actual).isEmpty();
    }

    @Test
    void findWeatherByRegionWhenExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Foo";
        Weather weather = new Weather(1L, new City("Test"), new WeatherType("sunshine"), 20.1, dateTime);
        when(biLoadingLRUCache.get(regionName, dateTime)).thenReturn(Optional.of(weather));
        Optional<Double> actual = weatherService.findWeatherByRegion(regionName, dateTime);

        assertThat(actual).contains(weather.getTemperature());
    }

    @Test
    void findWeatherWhenExistsButHasAnotherDateTime() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Foo";
        Weather weather = new Weather(1L, new City("Test"), new WeatherType("sunshine"), 20.1, dateTime);
        when(weatherDao.findByRegionName(regionName, dateTime.plusMinutes(1))).thenReturn(Optional.of(weather));

        Optional<Double> actual = weatherService.findWeatherByRegion(regionName, dateTime);

        assertThat(actual).isEmpty();
    }


    @Test
    void updateWeatherByRegionWhenExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                                .city(new City("Test"))
                                .weatherType(new WeatherType("sunshine"))
                                .date(dateTime)
                                .temperature(11.1)
                                .build();

        when(weatherDao.updateByRegionNameAndCreateIfNotExists(weather)).thenReturn(-1L);
        Optional<URI> actual = weatherService.updateWeatherByRegion(weather.getCity().getName(),
                new WeatherRequest(weather.getTemperature(), weather.getWeatherType().getType(), weather.getDate()));

        assertThat(actual).isEmpty();
    }

    @Test
    void updateWeatherByRegionWhenRegionNotExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                .city(new City("Test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(11.1)
                .build();

        URI uri = customUriBuilder.getUri(1L, dateTime);
        when(weatherDao.updateByRegionNameAndCreateIfNotExists(weather)).thenReturn(1L);

        Optional<URI> actual = weatherService.updateWeatherByRegion(weather.getCity().getName(),
                new WeatherRequest(weather.getTemperature(), weather.getWeatherType().getType(), weather.getDate()));

        assertThat(actual).contains(uri);
    }

    @Test
    void removeWeathersByRegionName() {
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        String regionName = "test";

        weatherService.removeWeathersByRegionName(regionName);
        verify(weatherDao).deleteByRegionName(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(regionName);

    }

    @Test
    void removeWeathersByRegionId() {
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        Long regionId = 5L;

        weatherService.removeWeathersByRegionId(regionId);
        verify(weatherDao).deleteByRegionId(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(regionId);

    }
}