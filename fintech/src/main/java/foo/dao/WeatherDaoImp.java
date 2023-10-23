package foo.dao;

import foo.models.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class WeatherDaoImp implements WeatherDao{
    private final AtomicLong nextWeatherId;
    private final AtomicLong nextCityId;
    private final AtomicLong nextTypeId;
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
        Optional<Long> typeId = weathers.stream()
                .filter(weather1 -> weather1.getWeatherType().getType().equals(weather.getWeatherType().getType()))
                .map(weather1 -> weather1.getWeatherType().getId())
                .findAny();

        if (typeId.isEmpty()) {
            weather.getWeatherType().setId(nextTypeId.getAndIncrement());
        } else {
            weather.getWeatherType().setId(typeId.get());
        }

        Optional<Long> cityId =  weathers.stream()
                .filter(weather1 -> weather1.getCity().getName().equals(weather.getCity().getName()))
                .map(weather1 -> weather1.getCity().getId())
                .findAny();

        if (cityId.isPresent()) {
            weather.getCity().setId(cityId.get());
        } else {
            weather.getCity().setId(nextCityId.getAndIncrement());
        }

        weather.setId(nextWeatherId.getAndIncrement());
        weathers.add(weather);
        return weather.getCity().getId();
    }

    @Override
    public Long saveWeatherAndType(Weather weather) {
        return saveWeatherWithNewRegion(weather);
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
                    .orElseGet(nextWeatherId::getAndIncrement);

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
        nextWeatherId = new AtomicLong(0L);
        nextCityId = new AtomicLong(0L);
        nextTypeId = new AtomicLong(0L);
        weathers = new CopyOnWriteArrayList<>();
    }
}