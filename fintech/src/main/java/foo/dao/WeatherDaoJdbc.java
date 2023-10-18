package foo.dao;

import foo.exceptions.CreateWeatherException;
import foo.exceptions.CustomSQLException;
import foo.exceptions.InvalidWeatherType;
import foo.exceptions.UpdateWeatherException;
import foo.models.Weather;
import foo.models.WeatherType;
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
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new CustomSQLException(e);
        }

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

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
            connection.commit();
            connection.setAutoCommit(true);

            return result;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new CustomSQLException(e);
            }

            throw new CustomSQLException(exception);
        }
    }

    @Override
    public Optional<Weather> findByRegionName(String name, LocalDateTime dateTime) {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new CustomSQLException(e);
        }

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

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
            connection.commit();
            connection.setAutoCommit(true);

            return result;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new CustomSQLException(e);
            }

            throw new CustomSQLException(exception);
        }
    }

    public Long saveCityIfNotExists(Connection connection, Weather weather) throws SQLException {
        String saveCitySql =
                "MERGE INTO city AS target USING (SELECT CAST(? AS VARCHAR)) AS source (name) ON (target.name = source.name) WHEN NOT MATCHED THEN INSERT (name) VALUES (source.name)";
        PreparedStatement createCity = connection.prepareStatement(saveCitySql, Statement.RETURN_GENERATED_KEYS);
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
        return cityId;
    }

    @Override
    public Long saveWeatherWithNewRegion(Weather weather) {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new CustomSQLException(e);
        }

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);
            Long cityId = saveCityIfNotExists(connection, weather);
            weather.getCity().setId(cityId);

            PreparedStatement statementForTypeId = connection.prepareStatement("SELECT id FROM weather_type WHERE type = ?");
            statementForTypeId.setString(1, weather.getWeatherType().getType());
            ResultSet resultSetForTypeId = statementForTypeId.executeQuery();

            weather.getWeatherType().setId(WeatherMapper.getId(resultSetForTypeId).orElseThrow(InvalidWeatherType::new));
            statementForTypeId.close();

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
            connection.commit();
            connection.setAutoCommit(true);

            return cityId;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new CustomSQLException(e);
            }

            throw new CustomSQLException(exception);
        }
    }


    @Override
    public Long saveWeatherAndType(Weather weather) {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new CustomSQLException(e);
        }

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);

            Long cityId = saveCityIfNotExists(connection, weather);
            weather.getCity().setId(cityId);
            PreparedStatement statementForTypeId = connection.prepareStatement("SELECT id FROM weather_type WHERE type = ?");
            statementForTypeId.setString(1, weather.getWeatherType().getType());
            ResultSet resultSetForTypeId = statementForTypeId.executeQuery();

            Optional<Long> optionalType = WeatherMapper.getId(resultSetForTypeId);
            if (optionalType.isEmpty()) {
                PreparedStatement typeStatement = connection.prepareStatement(
                        "INSERT INTO weather_type(type) VALUES (?)", Statement.RETURN_GENERATED_KEYS
                );

                typeStatement.setString(1, weather.getWeatherType().getType());
                if (typeStatement.executeUpdate() == 0) {
                    throw new CreateWeatherException();
                }

                Long typeId = WeatherMapper.getId(typeStatement.getGeneratedKeys()).orElseThrow(CreateWeatherException::new);
                weather.setWeatherType(new WeatherType(typeId, weather.getWeatherType().getType()));

                typeStatement.close();
            } else {
                weather.setWeatherType(new WeatherType(optionalType.get(), weather.getWeatherType().getType()));

            }

            statementForTypeId.close();

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
            connection.commit();
            connection.setAutoCommit(true);

            return cityId;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new CustomSQLException(e);
            }

            throw new CustomSQLException(exception);
        }
    }

    @Override
    public Long updateByRegionNameAndCreateIfNotExists(Weather temporaryWeather) {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new CustomSQLException(e);
        }

        try (connection) {
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
                    connection.prepareStatement("SELECT id from weather_type WHERE type = ?");
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
            connection.commit();
            connection.setAutoCommit(true);

            return -1L;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new CustomSQLException(e);
            }

            throw new CustomSQLException(exception);
        }
    }

    @Override
    public Boolean deleteByRegionId(Long regionId) {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new CustomSQLException(e);
        }

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM weather WHERE city_id = ?")) {
                preparedStatement.setLong(1, regionId);

                connection.commit();
                connection.setAutoCommit(true);
                return preparedStatement.executeUpdate() > 0;
            }

        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new CustomSQLException(e);
            }

            throw new CustomSQLException(exception);
        }
    }

    @Override
    public Boolean deleteByRegionName(String regionName) {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new CustomSQLException(e);
        }


        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            PreparedStatement preparedStatementForRegionId = connection.prepareStatement("SELECT id FROM city WHERE name = ?");
            preparedStatementForRegionId.setString(1, regionName);
            ResultSet resultSet = preparedStatementForRegionId.executeQuery();

            Optional<Long> cityId = WeatherMapper.getId(resultSet);
            preparedStatementForRegionId.close();

            connection.commit();
            connection.setAutoCommit(true);
            return cityId.isPresent() && deleteByRegionId(cityId.get());
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new CustomSQLException(e);
            }

            throw new CustomSQLException(exception);
        }
    }


}
