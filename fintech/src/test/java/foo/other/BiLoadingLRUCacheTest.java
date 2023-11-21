package foo.other;

import foo.dao.WeatherDaoJpa;
import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class BiLoadingLRUCacheTest {
    private Integer cacheSize;
    private BiLoadingLRUCache<Weather> biLoadingLRUCache;

    @Mock
    WeatherDaoJpa weatherDao;


    @BeforeAll
    void inin() {
        cacheSize = 3;
        biLoadingLRUCache = new BiLoadingLRUCache(3, 600L,
                (Object id, Object dateTime) -> weatherDao.findByRegionName((String) id, (LocalDateTime) dateTime));
    }

    @BeforeEach
    void clear() {
        biLoadingLRUCache.removeAll();
    }


    @Test
    void getWhenNotExists() {
        assertThat(biLoadingLRUCache.get("test")).isEmpty();
    }

    @Test
    void get() {
        Weather weather = Weather.builder()
                                .weatherType(new WeatherType(1L, "test"))
                                .city(new City(1L, "test"))
                                .temperature(10.2)
                                .build();

        biLoadingLRUCache.put(weather.getCity().getName(), weather);

        assertThat(biLoadingLRUCache.get("test").get()).isEqualTo(weather);
    }

    @Test
    void getAutoInsert() {
        Weather weather = Weather.builder()
                .weatherType(new WeatherType(1L, "test"))
                .city(new City(1L, "test"))
                .temperature(10.2)
                .date(LocalDateTime.now())
                .build();

        when(weatherDao.findByRegionName(weather.getCity().getName(), weather.getDate())).thenReturn(Optional.of(weather));
        biLoadingLRUCache.get(weather.getCity().getName(), weather.getDate());

        assertThat(biLoadingLRUCache.get("test").get()).isEqualTo(weather);
    }

    @Test
    void put() {
        Weather weather = null;
        for (long i = 0; i <= cacheSize; i++) {
            weather = Weather.builder()
                    .weatherType(new WeatherType(1L, "test"))
                    .city(new City(0L, "test"))
                    .temperature(10.2)
                    .build();
            weather.getCity().setId(i);
            weather.getCity().setName("test" + i);
            biLoadingLRUCache.put(weather.getCity().getName(), weather);
        }


        assertThat(biLoadingLRUCache.get(weather.getCity().getName()).get()).isEqualTo(weather);
        assertThat(biLoadingLRUCache.get("test")).isEmpty();
    }

    @Test
    void addWeatherMultipleThread() throws InterruptedException {
        assertThat(biLoadingLRUCache.getSize()).isZero();

        List<Thread> threadList = new ArrayList<>();
        for (long i = 1; i  < 4 + cacheSize; i++) {
            threadList.add(new Thread((new Runnable() {
                Long i;
                public Runnable init(Long i) {
                    this.i = i;
                    return this;
                }

                public void run() {
                    Weather weather = Weather.builder()
                            .weatherType(new WeatherType(1L, "test"))
                            .city(new City(i, "test" + i))
                            .temperature(10.2)
                            .build();

                    biLoadingLRUCache.put(weather.getCity().getName(), weather);
                }
            }).init(i)));

            threadList.get(threadList.size() - 1).start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }

        assertThat(biLoadingLRUCache.getSize()).isEqualTo(cacheSize);
    }

    @Test
    void removeFromCache() {
        Weather weather = Weather.builder()
                .weatherType(new WeatherType(1L, "test"))
                .city(new City(1L, "test"))
                .temperature(10.2)
                .build();

        biLoadingLRUCache.put(weather.getCity().getName(), weather);
        assertThat(biLoadingLRUCache.get("test")).isPresent();

        biLoadingLRUCache.removeFromCache("test");
        assertThat(biLoadingLRUCache.get("test")).isEmpty();
    }

}