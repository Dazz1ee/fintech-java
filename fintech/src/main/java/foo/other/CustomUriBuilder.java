package foo.other;

import java.net.URI;
import java.time.LocalDateTime;

public interface CustomUriBuilder {
    URI getUri(Long regionId, LocalDateTime dateTime);
}
