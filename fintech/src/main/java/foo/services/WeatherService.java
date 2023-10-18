package foo.services;

import foo.dao.WeatherDao;
import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherRequest;
import foo.models.WeatherType;
import foo.other.CustomUriBuilder;
import lombok.Value;
import org.springframework.stereotype.Service;
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

    public URI addNewRegionWithWeather(String cityName, WeatherRequest weatherWithNewRegion) {
        Weather weather = Weather.builder()
                .city(new City(cityName))
                .weatherType(new WeatherType(weatherWithNewRegion.weatherType().toLowerCase()))
                .date(weatherWithNewRegion.dateTime())
                .temperature(weatherWithNewRegion.temperature())
                .build();

        return CustomUriBuilder.getUriWeatherByRegionId(
                weatherDao.saveWeatherWithNewRegion(weather),
                weatherWithNewRegion.dateTime());
    }

    public Optional<URI> updateWeatherByRegion(String name, WeatherRequest weatherRequest) {
        Weather weather = Weather.builder()
                .city(new City(name))
                .weatherType(new WeatherType(weatherRequest.weatherType().toLowerCase()))
                .date(weatherRequest.dateTime())
                .temperature(weatherRequest.temperature())
                .build();

        Long regionId = weatherDao.updateByRegionNameAndCreateIfNotExists(weather);

        if(regionId == -1) {
            return Optional.empty();
        }
        return Optional.of(CustomUriBuilder.getUriWeatherByRegionId(regionId, weather.getDate()));
    }

    public  void removeWeathersByRegionId(Long regionId) {
        weatherDao.deleteByRegionId(regionId);
    }

    public void removeWeathersByRegionName(String name) {
        weatherDao.deleteByRegionName(name);
    }


}
