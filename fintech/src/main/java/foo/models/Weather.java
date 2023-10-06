package foo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Builder
public class Weather {
    private Long regionId;

    private String regionName;

    private Double temperature;

    private LocalDateTime date;
}
