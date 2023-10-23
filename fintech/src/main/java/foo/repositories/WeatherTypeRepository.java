package foo.repositories;

import foo.models.WeatherType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WeatherTypeRepository extends JpaRepository<WeatherType, Long> {
    @Query("SELECT w FROM WeatherType w where w.type = LOWER(:type)")
    Optional<WeatherType> findByType(@Param("type") String type);

}
