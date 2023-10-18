package foo.repositories;

import foo.models.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long>{

    @Query("SELECT w FROM Weather w JOIN w.city JOIN w.weatherType where  w.date = :date and w.city.id = :id")
    Optional<Weather> findByCityIdAndDate(@Param("id") Long id, @Param("date") LocalDateTime dateTime);
    @Query("SELECT w FROM Weather w JOIN w.city JOIN w.weatherType where  w.date = :date and w.city.name = :name")
    Optional<Weather> findByCityNameAndDate(@Param("name") String name, @Param("date") LocalDateTime dateTime);

    @Query("SELECT w.id FROM Weather w JOIN w.city JOIN w.weatherType where  w.date = :date and w.city.name = :name")
    Optional<Long> getIdByDateAndCityName(@Param("name") String name, @Param("date") LocalDateTime dateTime);

    Long deleteByCityName(String name);

    Long deleteByCityId(Long id);

}
