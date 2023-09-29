package foo.fintech.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record WeatherRequest(Double temperature,
                             @DateTimeFormat(pattern = "HH:mm dd/MM/yyyy")
                             @JsonFormat(pattern = "HH:mm dd/MM/yyyy")
                             @Schema(example = "00:30 10/10/2023", description = "DateTime format 'HH:mm dd/MM/yyyy'") LocalDateTime dateTime) {
}
