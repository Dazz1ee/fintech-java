package foo.repositories;

import foo.models.City;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    @Query("SELECT c.id FROM City c where c.name = :name")
    Optional<Long> findByName(@Param("name") String name);
}
