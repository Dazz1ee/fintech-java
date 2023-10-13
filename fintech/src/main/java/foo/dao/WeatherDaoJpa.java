package foo.dao;

import foo.exceptions.CreateWeatherException;
import foo.exceptions.InvalidWeatherType;
import foo.models.City;
import foo.models.Weather;
import foo.repositories.CityRepository;
import foo.repositories.WeatherRepository;
import foo.repositories.WeatherTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@ConditionalOnProperty(value = "hibernate.enabled", havingValue = "true")
@RequiredArgsConstructor
public class WeatherDaoJpa implements WeatherDao{
    private final WeatherRepository weatherRepository;
    private final CityRepository cityRepository;
    private final WeatherTypeRepository typeRepository;
    @Override
    public Optional<Weather> findByRegionId(Long id, LocalDateTime dateTime) {
        return weatherRepository.findByCityIdAndDate(id, dateTime);
    }

    @Override
    public Optional<Weather> findByRegionName(String name, LocalDateTime dateTime) {
        return weatherRepository.findByCityNameAndDate(name, dateTime);
    }

    @Override
    public Long saveWeatherWithNewRegion(Weather weather) {
        Optional<Long> cityId  = cityRepository.findByName(weather.getCity().getName());
        cityId.ifPresent(id -> weather.getCity().setId(id));
        City city = cityRepository.save(weather.getCity());
        weather.setCity(city);

        weather.setWeatherType(typeRepository.findByType(weather.getWeatherType().getType()).orElseThrow(InvalidWeatherType::new));

        try {
            return weatherRepository.save(weather).getCity().getId();
        } catch (RuntimeException e) {
            throw new CreateWeatherException();
        }

    }

    @Override
    public Long updateByRegionNameAndCreateIfNotExists(Weather weather) {
        Optional<Long> id = weatherRepository.getIdByDateAndCityName(weather.getCity().getName(), weather.getDate());
        id.ifPresent(weather::setId);
        Long cityId = saveWeatherWithNewRegion(weather);
        return id.isPresent() ? -1L : cityId;
    }

    @Override
    public Boolean deleteByRegionName(String regionName) {
        return weatherRepository.deleteByCityName(regionName) > 0;
    }

    @Override
    public Boolean deleteByRegionId(Long regionId) {
        return weatherRepository.deleteByCityId(regionId) > 0;
    }
}
