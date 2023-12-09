package foo.configurations;

import foo.dao.WeatherDao;
import foo.services.AverageListeningService;
import foo.services.ClientWeather;
import foo.services.SlidingAverageService;
import foo.services.WeatherApiService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.LinkedList;
import java.util.List;

@Setter
@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "sliding-average")
@Profile("!test")
public class SlidingAverageConfig {
    private List<String> cities;
    @Value("${spring.kafka.topic-name}")
    private String topic;

    @Bean
    public SlidingAverageService slidingAverageService(KafkaTemplate<String, String> kafkaTemplate,
                                                       WeatherApiService weatherApiService) {
        return new SlidingAverageService(kafkaTemplate,
                new LinkedList<>(cities),
                topic,
                weatherApiService);
    }

    @Bean
    public AverageListeningService listeningService(WeatherDao weatherDao) {
        return new AverageListeningService(weatherDao);
    }
}
