package foo.services;

import foo.models.WeatherApiResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ClientWeather {
    private final RestTemplate restTemplate;

    public ClientWeather(@Qualifier("weatherTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RateLimiter(name = "weatherApi")
    public ResponseEntity<WeatherApiResponse> getCurrentWeatherByRegion(String regionName) {
        return restTemplate.exchange("/current.json?q={regionName}",
                HttpMethod.GET,
                null,
                WeatherApiResponse.class,
                regionName);
    }
}
