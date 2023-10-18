package foo.services;

import foo.dao.WeatherDao;
import foo.exceptions.UnknownWeatherApiException;
import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherApiResponse;
import foo.models.WeatherType;
import foo.other.CustomUriBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;


@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherApiService {
    private final ClientWeather clientWeather;
    private final WeatherDao weatherDao;


    public ResponseEntity<WeatherApiResponse> getCurrentWeatherByRegion(String regionName) {
        return clientWeather.getCurrentWeatherByRegion(regionName);

    }

    public URI saveWeather(String regionName) {
        WeatherApiResponse weatherApiResponse =  clientWeather.getCurrentWeatherByRegion(regionName).getBody();
        if (weatherApiResponse == null) {
            throw new UnknownWeatherApiException();
        }
        Weather weather = Weather.builder()
                .city(new City(weatherApiResponse.location().name()))
                .weatherType(new WeatherType(weatherApiResponse.current().condition().text().toLowerCase()))
                .temperature(weatherApiResponse.current().tempC())
                .date(weatherApiResponse.current().lastUpdated())
                .build();

        Long id = weatherDao.saveWeatherAndType(weather);
        return CustomUriBuilder.getUriWeatherByRegionId(id , weather.getDate());
    }

}
