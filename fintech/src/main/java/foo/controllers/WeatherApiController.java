package foo.controllers;

import foo.models.CityNameRequest;
import foo.models.WeatherApiResponse;
import foo.services.WeatherApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/remote/api/weather")
@Tag(name = "WeatherApiController", description = "Controller for testing Weather API")
public class WeatherApiController {
    private final WeatherApiService weatherApiService;

    @Operation(summary = "Get current temperature from API")
    @GetMapping("/{regionName}")
    public ResponseEntity<WeatherApiResponse> getWeather(@PathVariable String regionName) {
        return weatherApiService.getCurrentWeatherByRegion(regionName);
    }

    @Operation(summary = "Save current temperature from API")
    @PostMapping()
    public ResponseEntity<Void> saveWeather(@RequestBody CityNameRequest cityNameRequest) {
        return ResponseEntity.created(weatherApiService.saveWeather(cityNameRequest.cityName())).build();
    }
}
