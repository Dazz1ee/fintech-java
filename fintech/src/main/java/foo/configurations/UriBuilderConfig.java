package foo.configurations;

import foo.other.CustomUriBuilder;
import foo.other.WeatherApiUriBuilder;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Setter
@Configuration
@ConfigurationProperties(prefix = "server")
public class UriBuilderConfig {

    private Integer port;

    private String host;

    private String schema;

    @Bean("uriBuilderForWeatherApi")
    public CustomUriBuilder customUriBuilder() {
        return new WeatherApiUriBuilder(schema, host, port);
    }
}
