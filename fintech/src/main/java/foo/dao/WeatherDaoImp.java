package foo.dao;

import foo.models.Weather;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class WeatherDaoImp implements WeatherDao{
    private final AtomicLong nextId;
    private final List<Weather> weathers;

    @Override
    public Optional<Weather> findByRegionId(Long id, LocalDateTime dateTime) {
        return weathers.parallelStream()
                .filter(weather -> weather.getDate().equals(dateTime) && weather.getCity().getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Weather> findByRegionName(String name, LocalDateTime dateTime) {
        return weathers.parallelStream()
                .filter(weather -> weather.getDate().equals(dateTime) && weather.getCity().getName().equals(name))
                .findFirst();
    }

    @Override
    public Long saveWeatherWithNewRegion(Weather weather) {
        weather.getCity().setId(nextId.getAndIncrement());
        synchronized (weathers) {
            weathers.add(weather);
        }
        return weather.getCity().getId();
    }

    @Override
    public Long updateByRegionNameAndCreateIfNotExists(Weather temporaryWeather) {
        Optional<Weather> weather = weathers.stream()
                .filter(temporary -> temporary.getDate()
                        .equals(temporaryWeather.getDate()) &&
                        temporary.getCity().getName().equals(temporaryWeather.getCity().getName()))
                .findFirst();

        if (weather.isPresent()) {
            weather.get().setTemperature(temporaryWeather.getTemperature());
        } else {
            Long regionId = weathers.stream()
                    .filter(temporary -> temporary.getCity().getName().equals(temporaryWeather.getCity().getName()))
                    .map(element -> element.getCity().getId()).findFirst()
                    .orElseGet(nextId::getAndIncrement);

            temporaryWeather.getCity().setId(regionId);
            weathers.add(temporaryWeather);
            return regionId;
        }

        return -1L;
    }

    @Override
    public Boolean deleteByRegionName(String regionName) {
        return weathers.removeIf(element -> element.getCity().getName().equals(regionName));
    }

    @Override
    public Boolean deleteByRegionId(Long regionId) {
        return weathers.removeIf(element -> element.getCity().getId().equals(regionId));
    }

    public WeatherDaoImp() {
        nextId = new AtomicLong(0L);
        weathers = new ArrayList<>();
    }
}