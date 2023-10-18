package foo.configurations;

import foo.services.WeatherApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
public class ApplicationRunnerConfig {
    private final WeatherApiService weatherApiService;

    @Bean
    @Profile("!test")
    public ApplicationRunner applicationRunner() {
        return args -> {
            weatherApiService.saveWeather("Moscow");
            weatherApiService.saveWeather("Saint Petersburg");
            weatherApiService.saveWeather("Samara");
            weatherApiService.saveWeather("Nizhny Novgorod");
            weatherApiService.saveWeather("Saratov");
            weatherApiService.saveWeather("London");
            weatherApiService.saveWeather("New York");
            weatherApiService.saveWeather("Tokyo");
            weatherApiService.saveWeather("Berlin");
        };
    }
}
