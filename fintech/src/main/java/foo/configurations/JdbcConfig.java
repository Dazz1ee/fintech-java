package foo.configurations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(value = "hibernate.enabled", havingValue = "false")
public class JdbcConfig {

    @Bean(name = "customHikariConfig")
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test");
        config.setUsername("sa");
        config.setPassword("password");
        return config;
    }


    @Bean(name = "customDataSource")
    public DataSource customHikariDatasource() {
        return new HikariDataSource(hikariConfig());
    }


}
