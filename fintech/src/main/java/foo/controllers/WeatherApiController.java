package foo.controllers;

import foo.models.WeatherApiResponse;
import foo.services.ClientWeather;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/remote/api/weather")
@Tag(name = "WeatherApiController", description = "Controller for testing Weather API")
public class WeatherApiController {
    private final ClientWeather clientWeather;

    @Operation(summary = "Get current temperature")
    @GetMapping("/{regionName}")
    public ResponseEntity<WeatherApiResponse> getWeather(@PathVariable String regionName) {
        return clientWeather.getCurrentWeatherByRegion(regionName);
    }
}
