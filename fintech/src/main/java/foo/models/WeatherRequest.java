package foo.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "Weather from request")
public record WeatherRequest(Double temperature,
                             @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                             @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                             @Schema(example = "2023-12-12T10:50", description = "DateTime format 'yyyy-MM-dd'T'HH:mm'") LocalDateTime dateTime) {
}
