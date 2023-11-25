package foo.other;

import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class WeatherMapper {
    public static Optional<Weather> getWeather(ResultSet resultSet) throws SQLException {
        Optional<Weather> weather = Optional.empty();
        if (resultSet.next()){
            weather = Optional.of(new Weather(resultSet.getLong("weathers.id"),
                    new City(resultSet.getLong("cities.id"), resultSet.getString("cities.name")),
                    new WeatherType(resultSet.getLong("weather_types.id"), resultSet.getString("weather_types.type")),
                    resultSet.getDouble("weathers.temperature"),
                    resultSet.getObject("weathers.date_time", LocalDateTime.class)));
        }

        resultSet.close();
        return weather;

    }
    public static Optional<Long> getId (ResultSet resultSet) throws SQLException {
        Optional<Long> cityId = Optional.empty();
        if (resultSet.next()) {
            cityId = Optional.of(resultSet.getLong("id"));
        }

        resultSet.close();
        return cityId;
    }

    public static Optional<Double> getAverage(ResultSet resultSet) throws SQLException {
        Optional<Double> average = Optional.empty();
        if (resultSet.next()) {
           average = Optional.of(resultSet.getDouble(1));
        }

        resultSet.close();
        return average;
    }
}
