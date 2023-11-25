package foo.dao;

import foo.models.Weather;
import java.time.LocalDateTime;
import java.util.Optional;

public interface WeatherDao {
    Optional<Weather> findByRegionId(Long id, LocalDateTime dateTime);
    Optional<Weather> findByRegionName(String name, LocalDateTime dateTime);

    Long saveWeatherWithNewRegion(Weather weather);

    Long saveWeatherAndType(Weather weather);

    Long updateByRegionNameAndCreateIfNotExists(Weather weather);

    Boolean deleteByRegionName(String regionName);
    Boolean deleteByRegionId(Long regionId);

    Double getAverageByCity(String city);
}
