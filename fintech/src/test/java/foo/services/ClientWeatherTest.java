package foo.services;

import foo.models.WeatherApiResponse;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ClientWeatherTest {
    @MockBean(name = "weatherTemplate")
    RestTemplate weatherTemplate;

    @Autowired
    ClientWeather clientWeather;

    @Test
    void getCurrentWeatherByRegion() {
        ResponseEntity<WeatherApiResponse> responseEntity = ResponseEntity.ok().build();
        when(weatherTemplate.exchange(any(String.class),
                eq(HttpMethod.GET),
                eq(null),
                eq(WeatherApiResponse.class),
                eq("test"))).thenReturn(responseEntity);

        for (int i = 0; i < 5; i++) {
            assertThat(clientWeather.getCurrentWeatherByRegion("test").getStatusCode()).isEqualTo(HttpStatus.OK);
        }

    }

    @Test
    void getCurrentWeatherByRegionWhenReturnedException() {
        ResponseEntity<WeatherApiResponse> responseEntity = ResponseEntity.ok().build();
        when(weatherTemplate.exchange(any(String.class),
                eq(HttpMethod.GET),
                eq(null),
                eq(WeatherApiResponse.class),
                eq("test"))).thenReturn(responseEntity);
        try {
            for (int i = 0; i < 11; i++) {
                assertThat(clientWeather.getCurrentWeatherByRegion("test").getStatusCode()).isEqualTo(HttpStatus.OK);
            }
            fail("Call must end");
        } catch (RuntimeException ex) {
            assertThat(ex.getClass()).isEqualTo(RequestNotPermitted.class);
        }
    }
}