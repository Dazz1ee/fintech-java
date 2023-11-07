package foo.dao;

import foo.exceptions.*;
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
        Connection connection = getConnection();

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            String findByRegionSql =
                    "SELECT cities.id, cities.name, weathers.id,  weathers.date_time, weathers.temperature, weather_types.id, weather_types.type " +
                            "FROM weathers JOIN cities ON weathers.city_id = cities.id " +
                            "JOIN weather_types ON weathers.type_id = weather_types.id " +
                            "WHERE weathers.city_id = ? AND weathers.date_time = ? LIMIT 1";

            PreparedStatement preparedStatement = connection.prepareStatement(findByRegionSql);
            preparedStatement.setLong(1, id);
            preparedStatement.setObject(2, dateTime);
            ResultSet resultSet = preparedStatement.executeQuery();
            Optional<Weather> result = WeatherMapper.getWeather(resultSet);

            preparedStatement.close();
            connection.commit();

            return result;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RollbackException(e);
            }

            throw new UnknownSQLException(exception);
        }
    }

    @Override
    public Optional<Weather> findByRegionName(String name, LocalDateTime dateTime) {
        Connection connection = getConnection();

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            String findByRegionSql =
                    "SELECT cities.id, cities.name, weathers.id,  weathers.date_time, weathers.temperature, weather_types.id, weather_types.type " +
                            "FROM cities JOIN weathers ON cities.id = weathers.city_id " +
                            "JOIN weather_types ON weathers.type_id = weather_types.id " +
                            "WHERE cities.name = ? and weathers.date_time = ? LIMIT 1";

            PreparedStatement preparedStatement = connection.prepareStatement(findByRegionSql);
            preparedStatement.setString(1, name);
            preparedStatement.setObject(2, dateTime);

            ResultSet resultSet = preparedStatement.executeQuery();
            Optional<Weather> result = WeatherMapper.getWeather(resultSet);

            preparedStatement.close();
            connection.commit();

            return result;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RollbackException(e.initCause(exception));
            }

            throw new UnknownSQLException(exception);
        }
    }

    public Long saveCityIfNotExists(Connection connection, Weather weather) throws SQLException {
        String saveCitySql =
                "MERGE INTO cities AS target USING (SELECT CAST(? AS VARCHAR)) AS source (name) ON (target.name = source.name) WHEN NOT MATCHED THEN INSERT (name) VALUES (source.name)";
        PreparedStatement createCity = connection.prepareStatement(saveCitySql, Statement.RETURN_GENERATED_KEYS);
        createCity.setString(1, weather.getCity().getName());
        int createdRows = createCity.executeUpdate();
        Long cityId;

        if (createdRows == 0) {
            PreparedStatement getCityIdStatement = connection.prepareStatement("SELECT id FROM cities WHERE name = ?");
            getCityIdStatement.setString(1, weather.getCity().getName());
            ResultSet resultSet = getCityIdStatement.executeQuery();
            cityId = WeatherMapper.getId(resultSet).orElseThrow(() -> {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    return new RollbackException(e);
                }
                return new CreateWeatherException();
            });
            getCityIdStatement.close();
        } else {
            ResultSet resultSet = createCity.getGeneratedKeys();
            cityId = WeatherMapper.getId(resultSet).orElseThrow(() -> {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    return new RollbackException(e);
                }
                return new CreateWeatherException();
            });
        }

        createCity.close();
        return cityId;
    }

    @Override
    public Long saveWeatherWithNewRegion(Weather weather) {
        Connection connection = getConnection();

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);
            Long cityId = saveCityIfNotExists(connection, weather);
            weather.getCity().setId(cityId);

            PreparedStatement statementForTypeId = connection.prepareStatement("SELECT id FROM weather_types WHERE type = ?");
            statementForTypeId.setString(1, weather.getWeatherType().getType());
            ResultSet resultSetForTypeId = statementForTypeId.executeQuery();

            weather.getWeatherType().setId(WeatherMapper.getId(resultSetForTypeId).orElseThrow(() -> {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    throw new RollbackException(e);
                }

                return new InvalidWeatherType();
            }));
            statementForTypeId.close();

            String createNewWeatherSql =
                    "INSERT INTO weathers(type_id, city_id, temperature, date_time) VALUES(?, ?, ?, ?)";

            PreparedStatement preparedStatementNewWeather = connection.prepareStatement(createNewWeatherSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatementNewWeather.setLong(1, weather.getWeatherType().getId());
            preparedStatementNewWeather.setLong(2, weather.getCity().getId());
            preparedStatementNewWeather.setDouble(3, weather.getTemperature());
            preparedStatementNewWeather.setObject(4, weather.getDate());
            boolean isCreated = preparedStatementNewWeather.executeUpdate() != 0;


            if (!isCreated) {
                connection.rollback();
                throw new CreateWeatherException();
            }

            ResultSet weatherIdSet = preparedStatementNewWeather.getGeneratedKeys();
            weather.setId(WeatherMapper.getId(weatherIdSet).orElseThrow(() -> {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    return new RollbackException(e);
                }
                return new CreateWeatherException();
            }));

            preparedStatementNewWeather.close();
            connection.commit();

            return cityId;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RollbackException(e);
            }

            throw new UnknownSQLException(exception);
        }
    }


    @Override
    public Long saveWeatherAndType(Weather weather) {
        Connection connection = getConnection();

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);

            Long cityId = saveCityIfNotExists(connection, weather);
            weather.getCity().setId(cityId);
            PreparedStatement statementForTypeId = connection.prepareStatement("SELECT id FROM weather_types WHERE type = ?");
            statementForTypeId.setString(1, weather.getWeatherType().getType());
            ResultSet resultSetForTypeId = statementForTypeId.executeQuery();

            Optional<Long> optionalType = WeatherMapper.getId(resultSetForTypeId);
            if (optionalType.isEmpty()) {
                PreparedStatement typeStatement = connection.prepareStatement(
                        "INSERT INTO weather_types(type) VALUES (?)", Statement.RETURN_GENERATED_KEYS
                );

                typeStatement.setString(1, weather.getWeatherType().getType());
                if (typeStatement.executeUpdate() == 0) {
                    throw new CreateWeatherException();
                }

                Long typeId = WeatherMapper.getId(typeStatement.getGeneratedKeys()).orElseThrow(() -> {
                    try {
                        connection.rollback();
                    } catch (SQLException e) {
                        throw new RollbackException(e);
                    }

                    return new CreateWeatherException();
                });
                weather.setWeatherType(new WeatherType(typeId, weather.getWeatherType().getType()));

                typeStatement.close();
            } else {
                weather.setWeatherType(new WeatherType(optionalType.get(), weather.getWeatherType().getType()));

            }

            statementForTypeId.close();

            String createNewWeatherSql =
                    "INSERT INTO weathers(type_id, city_id, temperature, date_time) VALUES(?, ?, ?, ?)";

            PreparedStatement preparedStatementNewWeather = connection.prepareStatement(createNewWeatherSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatementNewWeather.setLong(1, weather.getWeatherType().getId());
            preparedStatementNewWeather.setLong(2, weather.getCity().getId());
            preparedStatementNewWeather.setDouble(3, weather.getTemperature());
            preparedStatementNewWeather.setObject(4, weather.getDate());

            boolean isCreated = preparedStatementNewWeather.executeUpdate() != 0;

            if (!isCreated) {
                connection.rollback();
                throw new CreateWeatherException();
            }

            ResultSet weatherIdSet = preparedStatementNewWeather.getGeneratedKeys();
            weather.setId(WeatherMapper.getId(weatherIdSet).orElseThrow(() -> {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    return new RollbackException(e);
                }
                return new CreateWeatherException();
            }));

            preparedStatementNewWeather.close();
            connection.commit();

            return cityId;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RollbackException(e);
            }

            throw new UnknownSQLException(exception);
        }
    }

    @Override
    public Long updateByRegionNameAndCreateIfNotExists(Weather temporaryWeather) {
        Connection connection = getConnection();

        try (connection) {
            PreparedStatement preparedStatementForWeatherId
                    = connection.prepareStatement("SELECT weathers.id AS id FROM weathers " +
                    "JOIN cities ON weathers.city_id = cities.id WHERE cities.name = ? and weathers.date_time = ? LIMIT 1");

            preparedStatementForWeatherId.setString(1, temporaryWeather.getCity().getName());
            preparedStatementForWeatherId.setObject(2, temporaryWeather.getDate());

            ResultSet resultSetWeatherId = preparedStatementForWeatherId.executeQuery();

            Optional<Long> weatherId = WeatherMapper.getId(resultSetWeatherId);
            preparedStatementForWeatherId.close();

            if (weatherId.isEmpty()) {
                Long cityId = saveWeatherWithNewRegion(temporaryWeather);
                connection.commit();
                return cityId;
            }

            PreparedStatement preparedStatementForNewWeatherType =
                    connection.prepareStatement("SELECT id from weather_types WHERE type = ?");
            preparedStatementForNewWeatherType.setString(1, temporaryWeather.getWeatherType().getType());
            ResultSet resultSetWeatherType = preparedStatementForNewWeatherType.executeQuery();

            Long typeId = WeatherMapper.getId(resultSetWeatherType).orElseThrow(() -> {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    throw new RollbackException(e);
                }

                return new InvalidWeatherType();
            });
            preparedStatementForNewWeatherType.close();

            PreparedStatement updateStatement = connection.prepareStatement("UPDATE weathers SET type_id = ?, temperature = ? WHERE id = ?");
            updateStatement.setLong(1, typeId);
            updateStatement.setDouble(2, temporaryWeather.getTemperature());
            updateStatement.setLong(3, weatherId.get());

            if (updateStatement.executeUpdate() == 0) {
                updateStatement.close();
                connection.rollback();
                throw new UpdateWeatherException();
            }

            updateStatement.close();
            connection.commit();

            return -1L;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RollbackException(e);
            }

            throw new UnknownSQLException(exception);
        }
    }

    @Override
    public Boolean deleteByRegionId(Long regionId) {
        Connection connection = getConnection();

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            Boolean isDeleted = false;
            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM weathers WHERE city_id = ?")) {
                preparedStatement.setLong(1, regionId);

                isDeleted = preparedStatement.executeUpdate() > 0;
            }

            connection.commit();
            return isDeleted;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new GetConnectionException(e);
            }

            throw new GetConnectionException(exception);
        }
    }

    @Override
    public Boolean deleteByRegionName(String regionName) {
        Connection connection = getConnection();

        try (connection) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            PreparedStatement preparedStatementForRegionId = connection.prepareStatement("SELECT id FROM cities WHERE name = ?");
            preparedStatementForRegionId.setString(1, regionName);
            ResultSet resultSet = preparedStatementForRegionId.executeQuery();

            Optional<Long> cityId = WeatherMapper.getId(resultSet);
            preparedStatementForRegionId.close();

            boolean result = cityId.isPresent() && deleteByRegionId(cityId.get());
            connection.commit();
            return result;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new GetConnectionException(e);
            }

            throw new GetConnectionException(exception);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new GetConnectionException(e);
        }

    }

}