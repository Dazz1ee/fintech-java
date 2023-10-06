package foo.controllers;
import foo.models.ReturnedTemperature;
import foo.models.WeatherRequest;
import foo.services.WeatherService;
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

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


@Tag(name = "Weather", description = "Controller for weather-related CRUD operations")
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(
            summary = "get temperature by region ID",
            description = "сan get temperature by region ID and date and time. If dateTime was null, dateTime is equal to the current time. DateTime format: 'yyyy-MM-ddTHH:mm'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Integer.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @GetMapping("/{cityId:^\\d+$}")
    public ResponseEntity<?> getTemperatureByRegionId(@PathVariable("cityId") Long regionId,
                                                      @RequestParam(required = false)
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                                                      @Parameter(description = "DateTime format: 'yyyy-MM-ddTHH:mm'") LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        }

        Optional<Double> result = weatherService.findWeatherByRegion(regionId, date);

        return result.map(temperature -> ResponseEntity.ok(new ReturnedTemperature(temperature)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "get temperature by region name",
            description = "сan get temperature by region name. DateTime format: 'yyyy-MM-ddTHH:mm'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Integer.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @GetMapping("/{city:^[a-zA-Zа-яА-Я-]+$}")
    public ResponseEntity<?> getTemperature(@PathVariable("city") String name,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                                            @Parameter(description = "DateTime format: 'yyyy-MM-ddTHH:mm'") LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        }

        Optional<Double> result = weatherService.findWeatherByRegion(name, date);
        return result.map(temperature -> ResponseEntity.ok(new ReturnedTemperature(temperature)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Add weather with new region",
            description = "In this case, the addition is always successful. DateTime format: 'yyyy-MM-ddTHH:mm'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @PostMapping("/{city}")
    public ResponseEntity<Void> addNewRegion(@PathVariable("city") String name,
                                          @RequestBody WeatherRequest request) {
        URI uri = weatherService.addNewRegionWithWeather(name, request);

        return ResponseEntity.created(uri).build();
    }

    @Operation(
            summary = "update region weather by time",
            description = "If weather object was not found, a new object will be created. DateTime format: 'yyyy-MM-ddTHH:mm'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema())})
    })
    @PutMapping("/{city}")
    public ResponseEntity<?> updateWeatherByRegionName(@PathVariable("city") String name,
                                                       @RequestBody WeatherRequest weatherRequest) {
        Optional<URI> uri = weatherService.updateWeatherByRegion(name, weatherRequest);
        return uri.map(value -> ResponseEntity.created(value).build())
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Delete all weather by region name",
            description = "If weather does not exists, it will return not found."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @DeleteMapping("/{city:^[a-zA-Zа-яА-Я\\-]+$}")
    public ResponseEntity<Void> deleteWeatherByRegionName(@PathVariable("city") String name) {
        weatherService.removeWeathersByRegionName(name);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete all weather by region id",
            description = "If weather does not exists, it will return not found"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @DeleteMapping("/{city:^[\\d]+$}")
    public ResponseEntity<Void> deleteWeatherByRegionId(@PathVariable("city") Long regionId) {
        weatherService.removeWeathersByRegionId(regionId);
        return ResponseEntity.noContent().build();
    }

}
