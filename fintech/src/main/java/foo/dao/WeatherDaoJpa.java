package foo.dao;

import foo.exceptions.CreateWeatherException;
import foo.exceptions.InvalidWeatherType;
import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;
import foo.other.WeatherCache;
import foo.repositories.CityRepository;
import foo.repositories.WeatherRepository;
import foo.repositories.WeatherTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class WeatherDaoJpa implements WeatherDao{
    private final WeatherRepository weatherRepository;

    private final CityRepository cityRepository;

    private final WeatherTypeRepository typeRepository;

    private final WeatherCache weatherCache;
    public Optional<Weather> findByRegionId(Long id, LocalDateTime dateTime) {
        Optional<Weather> weather = weatherCache.getWeatherFromCache(id);
        if (weather.isPresent()) {
            return weather;
        }

        weather = weatherRepository.findByCityIdAndDate(id, dateTime);
        weather.ifPresent(element ->
                weatherCache.addWeatherToCache(element, (w) -> w.getCity().getId()));

        return weather;
    }

    @Override
    public Optional<Weather> findByRegionName(String name, LocalDateTime dateTime) {
        Optional<Weather> weather = weatherCache.getWeatherFromCache(name);
        if (weather.isPresent()) {
            return weather;
        }

        weather = weatherRepository.findByCityNameAndDate(name, dateTime);

        weather.ifPresent(element ->
                weatherCache.addWeatherToCache(element, (weather1) -> weather1.getCity().getName()));

        return weather;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long saveWeatherWithNewRegion(Weather weather) {
        Optional<Long> cityId  = cityRepository.findByName(weather.getCity().getName());
        cityId.ifPresent(id -> weather.getCity().setId(id));
        City city = cityRepository.save(weather.getCity());
        weather.setCity(city);

        weather.setWeatherType(typeRepository.findByType(weather.getWeatherType().getType()).orElseThrow(InvalidWeatherType::new));

        try {
            return weatherRepository.save(weather).getCity().getId();
        } catch (RuntimeException e) {
            throw new CreateWeatherException(e);
        }

    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long saveWeatherAndType(Weather weather) {
        Optional<Long> cityId  = cityRepository.findByName(weather.getCity().getName());
        cityId.ifPresent(id -> weather.getCity().setId(id));
        City city = cityRepository.save(weather.getCity());
        weather.setCity(city);

        Optional<WeatherType> typeOptional  = typeRepository.findByType(weather.getWeatherType().getType());
        if (typeOptional.isPresent()) {
            weather.setWeatherType(typeOptional.get());
        } else {
            WeatherType weatherType = typeRepository.save(weather.getWeatherType());
            weather.setWeatherType(weatherType);
        }

        try {
            return weatherRepository.save(weather).getCity().getId();
        } catch (RuntimeException e) {
            throw new CreateWeatherException(e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long updateByRegionNameAndCreateIfNotExists(Weather weather) {
        Optional<Long> id = weatherRepository.getIdByDateAndCityName(weather.getCity().getName(), weather.getDate());
        id.ifPresent(weather::setId);
        Long cityId = saveWeatherWithNewRegion(weather);

        weatherCache.removeFromCache(weather);
        return id.isPresent() ? -1L : cityId;
    }

    @Override
    public Boolean deleteByRegionName(String regionName) {
        weatherCache.removeFromCache(regionName);
        return weatherRepository.deleteByCityName(regionName) > 0;
    }

    @Override
    public Boolean deleteByRegionId(Long regionId) {
        weatherCache.removeFromCache(regionId);
        return weatherRepository.deleteByCityId(regionId) > 0;
    }
}
