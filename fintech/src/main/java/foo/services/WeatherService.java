package foo.services;

import foo.dao.WeatherDao;
import foo.models.Weather;
import foo.models.WeatherRequest;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Value
public class WeatherService {

    WeatherDao weatherDao;

    public Optional<Double> findWeatherByRegion(Long regionId, LocalDateTime dateTime) {
        return weatherDao.findByRegionId(regionId, dateTime).map(Weather::getTemperature);

    }

    public Optional<Double> findWeatherByRegion(String name, LocalDateTime dateTime) {
        return weatherDao.findByRegionName(name, dateTime).map(Weather::getTemperature);
    }

    public URI addNewRegionWithWeather(String regionName, WeatherRequest weatherWithNewRegion) {
        Weather weather = Weather.builder()
                .regionName(regionName)
                .date(weatherWithNewRegion.dateTime())
                .temperature(weatherWithNewRegion.temperature())
                .build();

        return getUriWeatherByRegionId(weatherDao.saveWeatherWithNewRegion(weather), weatherWithNewRegion.dateTime());
    }

    public Optional<URI> updateWeatherByRegion(String name, WeatherRequest weatherRequest) {
        Weather weather = Weather.builder()
                .regionName(name)
                .date(weatherRequest.dateTime())
                .temperature(weatherRequest.temperature())
                .build();

        Long regionId = weatherDao.updateByRegionNameAndCreateIfNotExists(weather);

        if(regionId == -1) {
            return Optional.empty();
        }
        return Optional.of(getUriWeatherByRegionId(regionId, weatherRequest.dateTime()));
    }

    public  void removeWeathersByRegionId(Long regionId) {
        weatherDao.deleteByRegionId(regionId);
    }

    public void removeWeathersByRegionName(String name) {
        weatherDao.deleteByRegionName(name);
    }

    private URI getUriWeatherByRegionId(Long regionId, LocalDateTime dateTime) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("/api/weather/{id}")
                .queryParam("date", dateTime)
                .buildAndExpand(regionId)
                .toUri();
    }



}
