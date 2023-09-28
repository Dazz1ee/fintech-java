package foo.fintech.models;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record WeatherRequest(Double temperature,
                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
                             @Schema(example = "2023-09-28T00:30", description = "DateTime format 'yyyy-MM-ddTHH:mm'") LocalDateTime dateTime) {
}
