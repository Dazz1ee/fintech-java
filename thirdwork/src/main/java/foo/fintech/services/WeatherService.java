package foo.fintech.services;

import foo.fintech.models.Weather;
import foo.fintech.models.WeatherRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@Service
public class WeatherService {

    private final AtomicLong nextId;
    private final List<Weather> weathers;
    public <T> Optional<Weather> findWeatherByRegion(T regionId, LocalDateTime dateTime, Function<Weather, T> extractor) {
        return weathers.parallelStream()
                .filter(weather -> weather.getDate().equals(dateTime) && extractor.apply(weather).equals(regionId))
                .findFirst();
    }

    public Long addNewRegionWithWeather(String regionName, WeatherRequest weatherWithNewRegion) {
        Weather weather = new Weather(nextId.getAndAdd(1L), regionName, weatherWithNewRegion.temperature(), weatherWithNewRegion.dateTime());
        weathers.add(weather);
        return weather.getRegionId();
    }

    public Long updateWeatherByRegion(String name, WeatherRequest weatherRequest) {
        Optional<Weather> weather = weathers.stream()
                .filter(temporary -> temporary.getDate().equals(weatherRequest.dateTime()) && temporary.getRegionName().equals(name))
                .findFirst();

        if (weather.isPresent()) {
            weather.get().setTemperature(weatherRequest.temperature());
        } else {
            Long regionId = weathers.stream()
                    .filter(temporary -> temporary.getRegionName().equals(name))
                    .map(Weather::getRegionId).findFirst()
                    .orElseGet(() -> nextId.getAndAdd(1L));

            weathers.add(new Weather(regionId, name, weatherRequest.temperature(), weatherRequest.dateTime()));
            return regionId;
        }

        return -1L;
    }

    public <T> Boolean removeWeathersByParameter(T parameter, Function<Weather, T> extract) {
        return weathers.removeIf(weather -> extract.apply(weather).equals(parameter));
    }

    public WeatherService() {
        nextId = new AtomicLong(0);
        weathers = new ArrayList<>();
    }
}
