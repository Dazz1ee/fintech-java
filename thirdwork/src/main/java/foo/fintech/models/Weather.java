package foo.fintech.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class Weather {
    private final Long regionId;

    private final String regionName;

    private Double temperature;

    private LocalDateTime date;
}
