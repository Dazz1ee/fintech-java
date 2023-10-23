package foo.other;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;


public abstract class CustomUriBuilder {
    private static String scheme;
    private static String host;

    private static Integer port;

    public static void setHost(String host) {
        if (CustomUriBuilder.host == null) {
            CustomUriBuilder.host = host;
        }
    }

    public static void setPort(Integer port) {
        if (CustomUriBuilder.port == null) {
            CustomUriBuilder.port = port;
        }
    }

    public static void setScheme(String scheme) {
        if (CustomUriBuilder.scheme == null) {
            CustomUriBuilder.scheme = scheme;
        }
    }

    public static URI getUriWeatherByRegionId(Long regionId, LocalDateTime dateTime) {
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
