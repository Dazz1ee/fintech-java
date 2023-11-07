package foo.configurations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import foo.dao.UserDao;
import foo.dao.UserDaoJdbc;
import foo.dao.WeatherDao;
import foo.dao.WeatherDaoJdbc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(value = "weather-dao-realization", havingValue = "jdbc")
public class JdbcConfig {
    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean(name = "customHikariConfig")
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
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

    @Bean(name = "jdbcUserDao")
    public UserDao userDao() {
        return new UserDaoJdbc(customHikariDatasource());
    }
}
