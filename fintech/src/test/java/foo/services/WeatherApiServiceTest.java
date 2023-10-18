package foo.services;

import foo.dao.WeatherDao;
import foo.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
class WeatherApiServiceTest {
    @Mock
    ClientWeather clientWeather;

    @Mock
    WeatherDao weatherDao;

    @InjectMocks
    WeatherApiService weatherApiService;

    @Test
    void getCurrentWeatherByRegion() {
        weatherApiService.getCurrentWeatherByRegion("Test");
        verify(clientWeather).getCurrentWeatherByRegion("Test");
    }

    @Test
    void saveWeather() {
        WeatherApiResponse weatherApiResponse = new WeatherApiResponse(new LocationResponse("Test", null,
                null, null, null, null, null, null),
                new CurrentResponse(null, null, null, null,
                        null, new CurrentResponse.Condition("Test", null, null), null,
                        null, null, null, null, null ,null ,
                        null, null, null, null, null, null,
                        null, null, null, null, null));

        ResponseEntity<WeatherApiResponse> returned = ResponseEntity.ok().body(weatherApiResponse);
        when(clientWeather.getCurrentWeatherByRegion("Test")).thenReturn(returned);
        ArgumentCaptor<Weather> argumentCaptor = ArgumentCaptor.forClass(Weather.class);

        weatherApiService.saveWeather("Test");
        verify(weatherDao).saveWeatherAndType(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getCity().getName()).isEqualTo(weatherApiResponse.location().name());

        assertThat(argumentCaptor.getValue().getWeatherType().getType())
                .isEqualTo(weatherApiResponse.current().condition().text().toLowerCase());
    }
}