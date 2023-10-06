package foo.dao;

import foo.models.Weather;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class WeatherDaoImp implements WeatherDao{
    private final AtomicLong nextId;
    private final List<Weather> weathers;

    @Override
    public Optional<Weather> findByRegionId(Long id, LocalDateTime dateTime) {
        return weathers.parallelStream()
                .filter(weather -> weather.getDate().equals(dateTime) && weather.getRegionId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Weather> findByRegionName(String name, LocalDateTime dateTime) {
        return weathers.parallelStream()
                .filter(weather -> weather.getDate().equals(dateTime) && weather.getRegionName().equals(name))
                .findFirst();
    }

    @Override
    public Long saveWeatherWithNewRegion(Weather weather) {
        weather.setRegionId(nextId.getAndIncrement());
        weathers.add(weather);
        return weather.getRegionId();
    }

    @Override
    public Long updateByRegionNameAndCreateIfNotExists(Weather temporaryWeather) {
        Optional<Weather> weather = weathers.stream()
                .filter(temporary -> temporary.getDate()
                        .equals(temporaryWeather.getDate()) &&
                        temporary.getRegionName().equals(temporaryWeather.getRegionName()))
                .findFirst();

        if (weather.isPresent()) {
            weather.get().setTemperature(temporaryWeather.getTemperature());
        } else {
            Long regionId = weathers.stream()
                    .filter(temporary -> temporary.getRegionName().equals(temporaryWeather.getRegionName()))
                    .map(Weather::getRegionId).findFirst()
                    .orElseGet(nextId::getAndIncrement);

            temporaryWeather.setRegionId(regionId);
            weathers.add(temporaryWeather);
            return regionId;
        }

        return -1L;
    }

    @Override
    public Boolean deleteByRegionName(String regionName) {
        return weathers.removeIf(element -> element.getRegionName().equals(regionName));
    }

    @Override
    public Boolean deleteByRegionId(Long regionId) {
        return weathers.removeIf(element -> element.getRegionId().equals(regionId));
    }

    public WeatherDaoImp() {
        nextId = new AtomicLong(0L);
        weathers = new ArrayList<>();
    }
}
