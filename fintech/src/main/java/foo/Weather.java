package foo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Weather {
    private final UUID regionId;

    private final String regionName;

    private final Double temperature;

    private final LocalDateTime date;

}
