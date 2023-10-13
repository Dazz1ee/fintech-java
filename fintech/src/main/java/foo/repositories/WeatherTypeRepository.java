package foo.repositories;

import foo.models.WeatherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@ConditionalOnProperty(value = "hibernate.enabled", havingValue = "true")
public interface WeatherTypeRepository extends JpaRepository<WeatherType, Long> {
    @Query("SELECT w FROM WeatherType w where w.type = LOWER(:type)")
    Optional<WeatherType> findByType(@Param("type") String type);
}
