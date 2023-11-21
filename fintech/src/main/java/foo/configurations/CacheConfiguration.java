package foo.configurations;

import foo.dao.WeatherDao;
import foo.models.Weather;
import foo.other.BiLoadingLRUCache;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;


@Configuration
@ConfigurationProperties("cache.course")
@Setter
public class CacheConfiguration {
    private Integer size;
    private Long validTime;

    @Bean
    public BiLoadingLRUCache<Weather> weatherCache(WeatherDao weatherDao) {
        return new BiLoadingLRUCache<>(size, validTime,
                (key, date) -> weatherDao.findByRegionName((String) key, (LocalDateTime) date));
    }

}
