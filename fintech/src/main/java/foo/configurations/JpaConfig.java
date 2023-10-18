package foo.configurations;

import foo.dao.WeatherDao;
import foo.dao.WeatherDaoJpa;
import foo.repositories.CityRepository;
import foo.repositories.WeatherRepository;
import foo.repositories.WeatherTypeRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(value = "weather-dao-realization", havingValue = "jpa")
@EnableJpaRepositories(basePackages = {"foo.repositories"})
public class JpaConfig {

    @Bean(name = "jpaDao")
    public WeatherDao weatherDao(WeatherRepository weatherRepository, WeatherTypeRepository weatherTypeRepository, CityRepository cityRepository) {
        return new WeatherDaoJpa(weatherRepository, cityRepository, weatherTypeRepository);
    }
}
