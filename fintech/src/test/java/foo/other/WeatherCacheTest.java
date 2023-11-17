package foo.other;

import foo.models.City;
import foo.models.Weather;
import foo.models.WeatherType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest
class WeatherCacheTest {
    @Value("${cache.course.size}")
    private Integer cacheSize;

    @Autowired
    private WeatherCache weatherCache;

    @BeforeEach
    void clear() {
        weatherCache.removeAll();
    }


    @Test
    void getWeatherFromCacheWhenNotExists() {
        assertThat(weatherCache.getWeatherFromCache(0L)).isEmpty();
    }

    @Test
    void getWeatherFromCache() {
        Weather weather = Weather.builder()
                                .weatherType(new WeatherType(1L, "test"))
                                .city(new City(1L, "test1"))
                                .temperature(10.2)
                                .build();

        weatherCache.addWeatherToCache(weather, w -> w.getCity().getId());

        assertThat(weatherCache.getWeatherFromCache(1L).get()).isEqualTo(weather);
    }

    @Test
    void getWeatherFromCacheWithName() {
        Weather weather = Weather.builder()
                .weatherType(new WeatherType(1L, "test"))
                .city(new City(2L, "test2"))
                .temperature(10.2)
                .build();

        weatherCache.addWeatherToCache(weather, w -> w.getCity().getName());

        assertThat(weatherCache.getWeatherFromCache("test2").get()).isEqualTo(weather);
    }




    @Test
    void addWeatherToCache() {
        Weather weather = null;
        for (long i = 0; i <= cacheSize; i++) {
            weather = Weather.builder()
                    .weatherType(new WeatherType(1L, "test"))
                    .city(new City(0L, "test0"))
                    .temperature(10.2)
                    .build();
            weather.getCity().setId(i);
            weather.getCity().setName("test" + i);
            weatherCache.addWeatherToCache(weather, w -> w.getCity().getName());
        }


        assertThat(weatherCache.getWeatherFromCache(weather.getCity().getName()).get()).isEqualTo(weather);
        assertThat(weatherCache.getWeatherFromCache("test0")).isEmpty();
    }

    @Test
    void addWeatherMultipleThread() throws InterruptedException {
        assertThat(weatherCache.getSize()).isZero();

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

                    weatherCache.addWeatherToCache(weather, w -> w.getCity().getId());
                }
            }).init(i)));

            threadList.get(threadList.size() - 1).start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }

        assertThat(weatherCache.getSize()).isEqualTo(cacheSize);
    }

    @Test
    void removeFromCache() {
        Weather weather = Weather.builder()
                .weatherType(new WeatherType(1L, "test"))
                .city(new City(1L, "test5"))
                .temperature(10.2)
                .build();

        weatherCache.addWeatherToCache(weather, w -> w.getCity().getId());
        assertThat(weatherCache.getWeatherFromCache(1L)).isPresent();

        weatherCache.removeFromCache(1L);
        assertThat(weatherCache.getWeatherFromCache(1L)).isEmpty();
    }

}