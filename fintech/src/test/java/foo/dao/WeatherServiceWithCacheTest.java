package foo.dao;

import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherRequest;
import foo.models.WeatherType;
import foo.other.CustomUriBuilder;
import foo.other.BiLoadingLRUCache;
import foo.services.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class WeatherServiceWithCacheTest {

    WeatherService weatherService;
    @Mock
    WeatherDaoJdbc weatherDao;

    BiLoadingLRUCache<Weather> biLoadingLRUCache;

    @Mock
    CustomUriBuilder customUriBuilder;

    @BeforeEach
    public void beforeEach()
    {
        BiFunction<Object, Object, Optional<Weather>> f =
                (Object id, Object dateTime) -> weatherDao.findByRegionName((String) id, (LocalDateTime) dateTime);
        biLoadingLRUCache = spy(new BiLoadingLRUCache<>(
                3, 600L, f));
        weatherService = new WeatherService(weatherDao, customUriBuilder, biLoadingLRUCache);
    }

    @Test
    void getWeatherWhenNotContainsInCache() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Weather weather = Weather.builder()
                .city(new City("test"))
                .weatherType(new WeatherType("sunshine"))
                .date(dateTime)
                .temperature(13.2)
                .build();


        when(weatherDao.findByRegionName(weather.getCity().getName(), weather.getDate()))
                .thenReturn(Optional.of(weather));

        weatherService.findWeatherByRegion("test", weather.getDate());

        verify(biLoadingLRUCache, times(1)).get(any(), any());
        verify(biLoadingLRUCache, times(1)).put(any(), any());
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
        weatherDao.saveWeatherWithNewRegion(weather);

        biLoadingLRUCache.put("Test2", weather);

        Optional<Double> actual = weatherService.findWeatherByRegion("Test2", dateTime);

        assertThat(actual.get()).isEqualTo(13.2);
        verify(biLoadingLRUCache, times(1)).get(any(), any());
        verify(biLoadingLRUCache, times(1)).put(any(), any());
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


        when(weatherDao.updateByRegionNameAndCreateIfNotExists(any())).thenReturn(-1L);

        weatherService.updateWeatherByRegion(weather.getCity().getName(), new WeatherRequest(
                weather.getTemperature(),
                weather.getWeatherType().getType(),
                weather.getDate()
        ));
        verify(biLoadingLRUCache, times(1)).removeFromCache(any());
    }

}
