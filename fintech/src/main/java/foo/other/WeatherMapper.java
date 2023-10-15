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
            weather = Optional.of(new Weather(resultSet.getLong("weather.id"),
                    new City(resultSet.getLong("city.id"), resultSet.getString("city.name")),
                    new WeatherType(resultSet.getLong("weather_type.id"), resultSet.getString("weather_type.type")),
                    resultSet.getDouble("weather.temperature"),
                    resultSet.getObject("weather.date_time", LocalDateTime.class)));
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
}
