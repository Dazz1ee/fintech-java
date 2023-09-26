package foo;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class Weather {
    UUID regionId;

    String regionName;

    Double temperature;

    LocalDateTime date;

}
