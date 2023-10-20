package foo.configurations;

import foo.other.CustomUriBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UriBuilderConfig {
    @Value("${server.port}")
    public void setPort(Integer port) {
        CustomUriBuilder.setPort(port);
    }

    @Value("${host}")
    public void setHost(String host) {
        CustomUriBuilder.setHost(host);
    }

    @Value("${schema}")
    public void setSchema(String schema) {
        CustomUriBuilder.setScheme(schema);
    }
}
