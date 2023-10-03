package foo.services;

import foo.dao.WeatherDao;
import foo.models.Weather;
import foo.models.WeatherRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
class WeatherServiceTest {

    @InjectMocks
    WeatherService weatherService;

    @Mock
    WeatherDao weatherDao;

    private URI createUri(LocalDateTime dateTime, Long regionId) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("/api/weather/{id}")
                .queryParam("date", dateTime)
                .buildAndExpand(regionId)
                .toUri();
    }

    @Test
    void addNewRegionWithWeatherWithSomeThreads() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Long regionId = 1L;
        WeatherRequest weatherRequest = new WeatherRequest(12.1, dateTime);
        Weather weather = Weather.builder()
                .date(dateTime)
                .temperature(12.1)
                .regionName("Test").build();
        when(weatherDao.saveWeatherWithNewRegion(weather)).thenReturn(regionId);

        URI uri = weatherService.addNewRegionWithWeather("Test", weatherRequest);

        assertThat(createUri(dateTime, regionId)).isEqualTo(uri);

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
        Weather weather = new Weather(1L, regionName, 20.1, dateTime);
        when(weatherDao.findByRegionName(regionName, dateTime)).thenReturn(Optional.of(weather));

        Optional<Double> actual = weatherService.findWeatherByRegion(regionName, dateTime);

        assertThat(actual).contains(weather.getTemperature());
    }

    @Test
    void findWeatherWhenExistsButHasAnotherDateTime() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Foo";
        Weather weather = new Weather(1L, regionName, 20.1, dateTime);
        when(weatherDao.findByRegionName(regionName, dateTime.plusMinutes(1))).thenReturn(Optional.of(weather));

        Optional<Double> actual = weatherService.findWeatherByRegion(regionName, dateTime);

        assertThat(actual).isEmpty();
    }


    @Test
    void updateWeatherByRegionWhenExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                                .regionName("Test")
                                .date(dateTime)
                                .temperature(11.1)
                                .build();

        when(weatherDao.updateByRegionNameAndCreateIfNotExists(weather)).thenReturn(-1L);
        Optional<URI> actual = weatherService.updateWeatherByRegion(weather.getRegionName(),
                new WeatherRequest(weather.getTemperature(), weather.getDate()));

        assertThat(actual).isEmpty();
    }

    @Test
    void updateWeatherByRegionWhenRegionNotExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                .regionName("Test")
                .date(dateTime)
                .temperature(11.1)
                .build();

        URI uri = createUri(dateTime, 1L);
        when(weatherDao.updateByRegionNameAndCreateIfNotExists(weather)).thenReturn(1L);

        Optional<URI> actual = weatherService.updateWeatherByRegion(weather.getRegionName(),
                new WeatherRequest(weather.getTemperature(), weather.getDate()));

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