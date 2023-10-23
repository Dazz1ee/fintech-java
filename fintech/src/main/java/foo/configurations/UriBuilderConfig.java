package foo.configurations;

import foo.other.CustomUriBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "server")
public class UriBuilderConfig {
    public void setPort(Integer port) {
        CustomUriBuilder.setPort(port);
    }
    
    public void setHost(String host) {
        CustomUriBuilder.setHost(host);
    }

    public void setSchema(String schema) {
        CustomUriBuilder.setScheme(schema);
    }
}
