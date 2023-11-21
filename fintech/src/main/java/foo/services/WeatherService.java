package foo.services;

import foo.dao.WeatherDao;
import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherRequest;
import foo.models.WeatherType;
import foo.other.CustomUriBuilder;
import foo.other.BiLoadingLRUCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WeatherService {

    private final WeatherDao weatherDao;

    private final CustomUriBuilder customUriBuilder;

    private final BiLoadingLRUCache<Weather> cache;

    public WeatherService(WeatherDao weatherDao, @Qualifier("uriBuilderForWeatherApi") CustomUriBuilder customUriBuilder, BiLoadingLRUCache<Weather> cache) {
        this.weatherDao = weatherDao;
        this.customUriBuilder = customUriBuilder;
        this.cache = cache;
    }

    public Optional<Double> findWeatherByRegion(Long regionId, LocalDateTime dateTime) {
        return cache
                .get(regionId, dateTime, (region, date) -> weatherDao.findByRegionId(regionId, dateTime))
                .map(Weather::getTemperature);

    }

    public Optional<Double> findWeatherByRegion(String name, LocalDateTime dateTime) {
        return cache.get(name, dateTime).map(Weather::getTemperature);
    }

    public URI addNewRegionWithWeather(String cityName, WeatherRequest weatherWithNewRegion) {
        Weather weather = Weather.builder()
                .city(new City(cityName))
                .weatherType(new WeatherType(weatherWithNewRegion.weatherType().toLowerCase()))
                .date(weatherWithNewRegion.dateTime())
                .temperature(weatherWithNewRegion.temperature())
                .build();

        return customUriBuilder.getUri(
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
            cache.removeFromCache(name);
            return Optional.empty();
        }

        return Optional.of(customUriBuilder.getUri(regionId, weather.getDate()));
    }

    public  void removeWeathersByRegionId(Long regionId) {
        cache.removeFromCache(regionId);
        weatherDao.deleteByRegionId(regionId);
    }

    public void removeWeathersByRegionName(String name) {
        cache.removeFromCache(name);
        weatherDao.deleteByRegionName(name);
    }


}
