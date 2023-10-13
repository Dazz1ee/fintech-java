package foo.dao;

import foo.exceptions.CreateWeatherException;
import foo.exceptions.CreatingConnectionException;
import foo.exceptions.InvalidWeatherType;
import foo.exceptions.UpdateWeatherException;
import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Qualifier("jdbcRepository")
@ConditionalOnProperty(value = "hibernate.enabled", havingValue = "false")
@Slf4j
public class WeatherDaoImp implements WeatherDao {
    private DataSource dataSource;

    public WeatherDaoImp(@Qualifier("customDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Weather> findByRegionId(Long id, LocalDateTime dateTime) {
        try (Connection connection = dataSource.getConnection()) {
            String findByRegionSql =
                    "SELECT * FROM weather JOIN city ON weather.city_id = city.id JOIN weather_type ON weather.type_id = weather_type.id WHERE weather.city_id = ? AND weather.date_time = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(findByRegionSql);
            preparedStatement.setLong(1, id);
            preparedStatement.setObject(2, dateTime);
            Optional<Weather> result;
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                result= Optional.of(new Weather(resultSet.getLong("weather.id"),
                        new City(resultSet.getLong("city.id"), resultSet.getString("city.name")),
                        new WeatherType(resultSet.getLong("weather_type.id"), resultSet.getString("weather_type.type")),
                        resultSet.getDouble("weather.temperature"),
                        resultSet.getObject("weather.date_time", LocalDateTime.class)));
            } else {
                result =  Optional.empty();
            }

            resultSet.close();
            preparedStatement.close();
            return result;
        } catch (SQLException e) {
            throw new CreatingConnectionException();
        }
    }

    @Override
    public Optional<Weather> findByRegionName(String name, LocalDateTime dateTime) {
        try (Connection connection = dataSource.getConnection()) {
            String findByRegionSql =
                    "SELECT * FROM city JOIN weather ON city.id = weather.city_id JOIN weather_type ON weather.type_id = weather_type.id WHERE city.name = ? and weather.date_time = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(findByRegionSql);
            preparedStatement.setString(1, name);
            preparedStatement.setObject(2, dateTime);
            Optional<Weather> result;

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                result = Optional.of(new Weather(resultSet.getLong("weather.id"),
                        new City(resultSet.getLong("city.id"), resultSet.getString("city.name")),
                        new WeatherType(resultSet.getLong("weather_type.id"), resultSet.getString("weather_type.type")),
                        resultSet.getDouble("weather.temperature"),
                        resultSet.getObject("weather.date_time", LocalDateTime.class)));
            } else {
                result = Optional.empty();
            }

            resultSet.close();
            preparedStatement.close();
            return result;
        } catch (SQLException e) {
            throw new CreatingConnectionException();
        }
    }

    @Override
    public Long saveWeatherWithNewRegion(Weather weather) {
        try (Connection connection = dataSource.getConnection()) {
            String saveCitySql =
                    "MERGE INTO city AS target USING (SELECT CAST(? AS VARCHAR)) AS source (name) ON (target.name = source.name) WHEN NOT MATCHED THEN INSERT (name) VALUES (source.name)";
            PreparedStatement createCity = connection.prepareStatement(saveCitySql, Statement.RETURN_GENERATED_KEYS);
            log.error(weather.getCity().getName());
            createCity.setString(1, weather.getCity().getName());
            int createdRows = createCity.executeUpdate();
            Long cityId;
            if (createdRows == 0) {
                PreparedStatement getCityIdStatement = connection.prepareStatement("SELECT id FROM city WHERE name = ?");
                getCityIdStatement.setString(1, weather.getCity().getName());
                ResultSet resultSet = getCityIdStatement.executeQuery();
                resultSet.next();
                cityId = resultSet.getLong("id");
                resultSet.close();
            } else {
                ResultSet resultSet = createCity.getGeneratedKeys();
                resultSet.next();
                cityId = resultSet.getLong("id");
                resultSet.close();
            }

            createCity.close();

            PreparedStatement statementForTypeId = connection.prepareStatement("SELECT id FROM weather_type WHERE type = LOWER(?)");
            statementForTypeId.setString(1, weather.getWeatherType().getType());
            ResultSet resultSetForTypeId = statementForTypeId.executeQuery();

            if (resultSetForTypeId.next()) {
                weather.getWeatherType().setId(resultSetForTypeId.getLong("id"));
            } else {
                log.debug(weather.getWeatherType().getType());
                throw new InvalidWeatherType();
            }

            weather.getCity().setId(cityId);
            String createNewWeatherSql =
                    "INSERT INTO weather(type_id, city_id, temperature, date_time) VALUES(?, ?, ?, ?)";

            PreparedStatement preparedStatementNewWeather = connection.prepareStatement(createNewWeatherSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatementNewWeather.setLong(1, weather.getWeatherType().getId());
            preparedStatementNewWeather.setLong(2, weather.getCity().getId());
            preparedStatementNewWeather.setDouble(3, weather.getTemperature());
            preparedStatementNewWeather.setObject(4, weather.getDate());

            boolean isCreated = preparedStatementNewWeather.executeUpdate() != 0;

            if (!isCreated) {
                throw new CreateWeatherException();
            }

            ResultSet weatherIdSet = preparedStatementNewWeather.getGeneratedKeys();
            weatherIdSet.next();
            weather.setId(weatherIdSet.getLong("id"));
            preparedStatementNewWeather.close();

            return cityId;
        } catch (SQLException e) {
            log.debug("{}", e.getMessage());
            throw new CreatingConnectionException();
        }
    }

    @Override
    public Long updateByRegionNameAndCreateIfNotExists(Weather temporaryWeather) {
        try (Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatementForWeatherId
                    = connection.prepareStatement("SELECT weather.id FROM weather JOIN city ON weather.city_id = city.id WHERE city.name = ? and weather.date_time = ?");
            preparedStatementForWeatherId.setString(1, temporaryWeather.getCity().getName());
            preparedStatementForWeatherId.setObject(2, temporaryWeather.getDate());

            ResultSet resultSetWeatherId = preparedStatementForWeatherId.executeQuery();

            if (!resultSetWeatherId.next()) {
                return saveWeatherWithNewRegion(temporaryWeather);
            }

            Long weatherId = resultSetWeatherId.getLong("weather.id");
            resultSetWeatherId.close();
            preparedStatementForWeatherId.close();

            PreparedStatement preparedStatementForNewWeatherType =
                    connection.prepareStatement("SELECT id from weather_type WHERE type = LOWER(?)");
            preparedStatementForNewWeatherType.setString(1, temporaryWeather.getWeatherType().getType());
            ResultSet resultSetWeatherType = preparedStatementForNewWeatherType.executeQuery();
            if (!resultSetWeatherType.next()) {
                throw new InvalidWeatherType();
            }

            Long typeId = resultSetWeatherType.getLong("id");
            resultSetWeatherType.close();
            preparedStatementForNewWeatherType.close();

            PreparedStatement updateStatement = connection.prepareStatement("UPDATE weather SET type_id = ?, temperature = ? WHERE id = ?");
            updateStatement.setLong(1, typeId);
            updateStatement.setDouble(2, temporaryWeather.getTemperature());
            updateStatement.setLong(3, weatherId);

            if (updateStatement.executeUpdate() == 0) {
                throw new UpdateWeatherException();
            }

            return -1L;
        } catch (SQLException e) {
            throw new CreatingConnectionException();
        }
    }

    @Override
    public Boolean deleteByRegionId(Long regionId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM weather WHERE city_id = ?");
            preparedStatement.setLong(1, regionId);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new CreatingConnectionException();
        }
    }

    @Override
    public Boolean deleteByRegionName(String regionName) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatementForRegionId = connection.prepareStatement("SELECT id FROM city WHERE name = ?");
            preparedStatementForRegionId.setString(1, regionName);
            ResultSet resultSet = preparedStatementForRegionId.executeQuery();

            return resultSet.next() && deleteByRegionId(resultSet.getLong("id"));

        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new CreatingConnectionException();
        }
    }


}
