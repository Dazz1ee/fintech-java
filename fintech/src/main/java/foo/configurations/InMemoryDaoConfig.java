package foo.configurations;

import foo.dao.WeatherDao;
import foo.dao.WeatherDaoImp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "weather-dao-realization", havingValue = "in-memory")
public class InMemoryDaoConfig {
    @Bean(name = "inMemoryDao")
    public WeatherDao weatherDao() {
        return new WeatherDaoImp();
    }
}
