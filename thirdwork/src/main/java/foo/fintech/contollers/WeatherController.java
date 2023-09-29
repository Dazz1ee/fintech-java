package foo.fintech.contollers;

import foo.fintech.models.Weather;
import foo.fintech.models.WeatherRequest;
import foo.fintech.services.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


@Tag(name = "Weather", description = "Controller for weather-related CRUD operations")
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(
            summary = "get temperature by region ID",
            description = "сan get temperature by region ID and date and time. If dateTime was null, dateTime is equal to the current time. DateTime format: 'HH:mm dd/MM/yyyy'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Integer.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @GetMapping("/{city:[\\d]+}")
    public ResponseEntity<?> getTemperatureByRegionId(@PathVariable("city") Long regionId,
                                                      @RequestParam(required = false)
                                                      @DateTimeFormat(pattern = "HH:mm dd/MM/yyyy")
                                                      @Parameter(description = "DateTime format: 'HH:mm dd/MM/yyyy'") LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        }

        log.debug("date = {}", date);

        Optional<Weather> result = weatherService.findWeatherByRegion(regionId, date, Weather::getRegionId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.get().getTemperature());
    }

    @Operation(
            summary = "get temperature by region name",
            description = "сan get temperature by region name. DateTime format: 'HH:mm dd/MM/yyyy'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Integer.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @GetMapping("/{city:[^\\d]+}")
    public ResponseEntity<?> getTemperature(@PathVariable("city") String name,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(pattern = "HH:mm dd/MM/yyyy")
                                            @Parameter(description = "DateTime format: 'HH:mm dd/MM/yyyy'") LocalDateTime date) {

        if (date == null) {
            date = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        }

        Optional<Weather> result = weatherService.findWeatherByRegion(name, date, Weather::getRegionName);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.get().getTemperature());
    }

    @Operation(
            summary = "Add weather with new region",
            description = "In this case, the addition is always successful. DateTime format: 'HH:mm dd/MM/yyyy'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @PostMapping("/{city}")
    public ResponseEntity<?> addNewRegion(@PathVariable("city") String name,
                                          @RequestBody WeatherRequest request) {
        Long id = weatherService.addNewRegionWithWeather(name, request);

        return ResponseEntity.created(getUriWeatherByRegionId(id, request.dateTime())).build();
    }

    @Operation(
            summary = "update region weather by time",
            description = "If weather object was not found, a new object will be created. DateTime format: 'HH:mm dd/MM/yyyy'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema())})
    })
    @PutMapping("/{city}")
    public ResponseEntity<?> updateWeatherByRegionName(@PathVariable("city") String name,
                                                       @RequestBody WeatherRequest weatherRequest) {
        Long regionId = weatherService.updateWeatherByRegion(name, weatherRequest);

        log.debug("id = {}, date = {}", regionId, weatherRequest.dateTime());

        if (regionId == - 1) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.created(getUriWeatherByRegionId(regionId, weatherRequest.dateTime())).build();
    }

    @Operation(
            summary = "Delete all weather by region name",
            description = "If weather does not exists, it will return not found."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @DeleteMapping("/{city:[^\\d]+}")
    public ResponseEntity<?> deleteWeatherByRegionName(@PathVariable("city") String name) {
        if (weatherService.removeWeathersByParameter(name, Weather::getRegionName)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Delete all weather by region id",
            description = "If weather does not exists, it will return not found"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @DeleteMapping("/{city:[\\d]+}")
    public ResponseEntity<?> deleteWeatherByRegionId(@PathVariable("city") Long regionId) {
        if(weatherService.removeWeathersByParameter(regionId, Weather::getRegionId)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    private URI getUriWeatherByRegionId(Long regionId, LocalDateTime dateTime) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("/api/weather/{id}")
                .queryParam("date", dateTime)
                .buildAndExpand(regionId)
                .toUri();
    }
}
