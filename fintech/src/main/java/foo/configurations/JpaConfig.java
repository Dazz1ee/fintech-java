package foo.configurations;

import foo.dao.UserDao;
import foo.dao.UserDaoJpa;
import foo.dao.WeatherDao;
import foo.dao.WeatherDaoJpa;
import foo.other.WeatherCache;
import foo.repositories.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(value = "weather-dao-realization", havingValue = "jpa")
@EnableJpaRepositories(basePackages = {"foo.repositories"})
public class JpaConfig {

    @Bean(name = "jpaDao")
    public WeatherDao weatherDao(WeatherRepository weatherRepository, WeatherTypeRepository
            weatherTypeRepository, CityRepository cityRepository, WeatherCache weatherCache) {
        return new WeatherDaoJpa(weatherRepository, cityRepository, weatherTypeRepository, weatherCache);
    }

    @Bean(name = "jpaUserDao")
    public UserDao userDao(UserRepository userRepository, RoleRepository roleRepository) {
        return new UserDaoJpa(userRepository, roleRepository);
    }
}
