package foo.services;

import foo.dao.WeatherDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@RequiredArgsConstructor
@Slf4j
public class AverageListeningService {
    private final WeatherDao weatherDao;

    @KafkaListener(topics = "${spring.kafka.topic-name}")
    public void calculateAverage(ConsumerRecord<String, String> city, Acknowledgment acknowledgment) {
        log.info("temperature = {}", weatherDao.getAverageByCity(city.value()));
        acknowledgment.acknowledge();
    }
}
