package foo.other;

import lombok.AllArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;


@AllArgsConstructor
public class WeatherApiUriBuilder implements CustomUriBuilder {

    private String scheme;

    private String host;

    private Integer port;


    public URI getUri(Long regionId, LocalDateTime dateTime) {
        return UriComponentsBuilder.newInstance()
                .scheme(scheme)
                .host(host)
                .port(port)
                .path("/api/weather/{id}")
                .queryParam("date", dateTime)
                .buildAndExpand(regionId)
                .toUri();
    }
}
