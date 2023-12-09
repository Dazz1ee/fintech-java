package foo.services;

import foo.exceptions.UnknownWeatherApiException;
import foo.models.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.Queue;

@RequiredArgsConstructor
public class SlidingAverageService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final Queue<String> queue;

    private final String topic;

    private final  WeatherApiService weatherApiService;


    @Scheduled(cron = "${sliding-average.cron.expression}")
    public void getNewWeather() {
        String cityName = queue.poll();
        queue.add(cityName);
        weatherApiService.saveWeather(cityName);
        kafkaTemplate.send(topic, cityName);
    }
}
