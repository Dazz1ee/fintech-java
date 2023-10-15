package foo.configurations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import foo.dao.WeatherDao;
import foo.dao.WeatherDaoJdbc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(value = "weather-dao-realization", havingValue = "jdbc")
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

    @Bean(name = "jdbcDao")
    public WeatherDao weatherDao() {
        return new WeatherDaoJdbc(customHikariDatasource());
    }
}
