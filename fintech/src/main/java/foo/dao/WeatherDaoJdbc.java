package foo.dao;

import foo.exceptions.CreateWeatherException;
import foo.exceptions.CreatingConnectionException;
import foo.exceptions.InvalidWeatherType;
import foo.exceptions.UpdateWeatherException;
import foo.models.Weather;
import foo.other.WeatherMapper;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;


@Slf4j
public class WeatherDaoJdbc implements WeatherDao {
    private final DataSource dataSource;

    public WeatherDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Weather> findByRegionId(Long id, LocalDateTime dateTime) {
        try (Connection connection = dataSource.getConnection()) {
            String findByRegionSql =
                    "SELECT city.id, city.name, weather.id,  weather.date_time, weather.temperature, weather_type.id, weather_type.type " +
                            "FROM weather JOIN city ON weather.city_id = city.id " +
                            "JOIN weather_type ON weather.type_id = weather_type.id " +
                            "WHERE weather.city_id = ? AND weather.date_time = ? LIMIT 1";

            PreparedStatement preparedStatement = connection.prepareStatement(findByRegionSql);
            preparedStatement.setLong(1, id);
            preparedStatement.setObject(2, dateTime);
            ResultSet resultSet = preparedStatement.executeQuery();
            Optional<Weather> result = WeatherMapper.getWeather(resultSet);

            preparedStatement.close();
            return result;
        } catch (SQLException exception) {
            throw new CreatingConnectionException(exception);
        }
    }

    @Override
    public Optional<Weather> findByRegionName(String name, LocalDateTime dateTime) {
        try (Connection connection = dataSource.getConnection()) {
            String findByRegionSql =
                    "SELECT city.id, city.name, weather.id,  weather.date_time, weather.temperature, weather_type.id, weather_type.type " +
                            "FROM city JOIN weather ON city.id = weather.city_id " +
                            "JOIN weather_type ON weather.type_id = weather_type.id " +
                            "WHERE city.name = ? and weather.date_time = ? LIMIT 1";

            PreparedStatement preparedStatement = connection.prepareStatement(findByRegionSql);
            preparedStatement.setString(1, name);
            preparedStatement.setObject(2, dateTime);

            ResultSet resultSet = preparedStatement.executeQuery();
            Optional<Weather> result = WeatherMapper.getWeather(resultSet);

            preparedStatement.close();
            return result;
        } catch (SQLException exception) {
            throw new CreatingConnectionException(exception);
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
                cityId = WeatherMapper.getId(resultSet).orElseThrow(CreateWeatherException::new);
            } else {
                ResultSet resultSet = createCity.getGeneratedKeys();
                cityId = WeatherMapper.getId(resultSet).orElseThrow(CreateWeatherException::new);
            }

            createCity.close();

            PreparedStatement statementForTypeId = connection.prepareStatement("SELECT id FROM weather_type WHERE type = LOWER(?)");
            statementForTypeId.setString(1, weather.getWeatherType().getType());
            ResultSet resultSetForTypeId = statementForTypeId.executeQuery();

            weather.getWeatherType().setId(WeatherMapper.getId(resultSetForTypeId).orElseThrow(InvalidWeatherType::new));
            statementForTypeId.close();

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
            weather.setId(WeatherMapper.getId(weatherIdSet).orElseThrow(CreateWeatherException::new));
            preparedStatementNewWeather.close();

            return cityId;
        } catch (SQLException exception) {
            throw new CreatingConnectionException(exception);
        }
    }

    @Override
    public Long updateByRegionNameAndCreateIfNotExists(Weather temporaryWeather) {
        try (Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatementForWeatherId
                    = connection.prepareStatement("SELECT weather.id AS id FROM weather " +
                    "JOIN city ON weather.city_id = city.id WHERE city.name = ? and weather.date_time = ? LIMIT 1");

            preparedStatementForWeatherId.setString(1, temporaryWeather.getCity().getName());
            preparedStatementForWeatherId.setObject(2, temporaryWeather.getDate());

            ResultSet resultSetWeatherId = preparedStatementForWeatherId.executeQuery();

            Optional<Long> weatherId = WeatherMapper.getId(resultSetWeatherId);
            preparedStatementForWeatherId.close();

            if (weatherId.isEmpty()) {
                return saveWeatherWithNewRegion(temporaryWeather);
            }

            PreparedStatement preparedStatementForNewWeatherType =
                    connection.prepareStatement("SELECT id from weather_type WHERE type = LOWER(?)");
            preparedStatementForNewWeatherType.setString(1, temporaryWeather.getWeatherType().getType());
            ResultSet resultSetWeatherType = preparedStatementForNewWeatherType.executeQuery();

            Long typeId = WeatherMapper.getId(resultSetWeatherType).orElseThrow(InvalidWeatherType::new);
            preparedStatementForNewWeatherType.close();

            PreparedStatement updateStatement = connection.prepareStatement("UPDATE weather SET type_id = ?, temperature = ? WHERE id = ?");
            updateStatement.setLong(1, typeId);
            updateStatement.setDouble(2, temporaryWeather.getTemperature());
            updateStatement.setLong(3, weatherId.get());

            if (updateStatement.executeUpdate() == 0) {
                updateStatement.close();
                throw new UpdateWeatherException();
            }

            updateStatement.close();
            return -1L;
        } catch (SQLException exception) {
            throw new CreatingConnectionException(exception);
        }
    }

    @Override
    public Boolean deleteByRegionId(Long regionId) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM weather WHERE city_id = ?")) {
                preparedStatement.setLong(1, regionId);
                return preparedStatement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            throw new CreatingConnectionException(exception);
        }
    }

    @Override
    public Boolean deleteByRegionName(String regionName) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatementForRegionId = connection.prepareStatement("SELECT id FROM city WHERE name = ?");
            preparedStatementForRegionId.setString(1, regionName);
            ResultSet resultSet = preparedStatementForRegionId.executeQuery();

            Optional<Long> cityId = WeatherMapper.getId(resultSet);
            preparedStatementForRegionId.close();
            return cityId.isPresent() && deleteByRegionId(cityId.get());

        } catch (SQLException exception) {
            throw new CreatingConnectionException(exception);
        }
    }


}
