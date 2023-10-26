package foo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foo.dao.WeatherDao;
import foo.exceptions.WrongLocationException;
import foo.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class WeatherApiServiceTest {
    @Autowired
    @Qualifier("weatherTemplate")
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper mapper;

    @SpyBean
    WeatherDao weatherDao;

    @SpyBean
    ClientWeather clientWeather;

    @Autowired
    WeatherApiService weatherApiService;

    @Value("${api_url}")
    private String url;

    @Value("${api_key}")
    private String apiKey;

    @Container
    public static GenericContainer<?> h2Container =
            new GenericContainer<>(DockerImageName.parse("oscarfonts/h2"))
                    .withExposedPorts(1521).withEnv("H2_OPTIONS", "-ifNotExists");

    static {
        h2Container.start();
    }

    @DynamicPropertySource
    static void setPropertySource(DynamicPropertyRegistry dynamicPropertySource) {
        dynamicPropertySource.add("spring.datasource.url",
                () -> String.format("jdbc:h2:tcp://%s:%d/test", h2Container.getHost(), h2Container.getMappedPort(1521)));
    }

    @BeforeEach
    public void initServer() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    private WeatherApiResponse getWeatherApiResponse(String location, String weatherType, Double temperature, LocalDateTime localDateTime) {
        return new WeatherApiResponse(new LocationResponse(location, location,
                null, null, null, null, null, null),
                new CurrentResponse(null, localDateTime, temperature, null,
                        null, new CurrentResponse.Condition(weatherType, null, null), null,
                        null, null, null, null, null ,null ,
                        null, null, null, null, null, null,
                        null, null, null, null, null));
    }

    @Test
    void getCurrentWeatherByRegion() throws URISyntaxException, JsonProcessingException {
        WeatherApiResponse response = getWeatherApiResponse("Test", "snowing", -5.1, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        mockServer.expect(
                        requestTo(new URI(String.format("%s/current.json?q=%s&key=%s", url, "Test", apiKey))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(response))
                );

        ResponseEntity<WeatherApiResponse> expected = weatherApiService.getCurrentWeatherByRegion("Test");
        verify(clientWeather).getCurrentWeatherByRegion("Test");
        assertThat(expected.getBody()).isEqualTo(response);
    }

    @Test
    void saveWeather() throws URISyntaxException, JsonProcessingException {
        LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        WeatherApiResponse weatherApiResponse = getWeatherApiResponse("Test", "snowing", -5.1, localDateTime);

        mockServer.expect(
                        requestTo(new URI(String.format("%s/current.json?q=%s&key=%s", url, "Test", apiKey))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(weatherApiResponse))
                );

        ArgumentCaptor<Weather> weatherArgumentCaptor = ArgumentCaptor.forClass(Weather.class);
        URI uri =  weatherApiService.saveWeather("Test");

        assertThat(uri).isNotNull();
        verify(clientWeather).getCurrentWeatherByRegion("Test");
        verify(weatherDao).saveWeatherAndType(weatherArgumentCaptor.capture());

        Weather capturedWeather = weatherArgumentCaptor.getValue();
        assertThat(capturedWeather.getCity().getName()).isEqualTo("Test");
        assertThat(capturedWeather.getDate()).isEqualTo(localDateTime);
        assertThat(capturedWeather.getTemperature()).isEqualTo(-5.1);
        assertThat(capturedWeather.getWeatherType().getType()).isEqualTo("snowing");

        Optional<Weather> weather = weatherDao.findByRegionName("Test", localDateTime);
        assertThat(weather).isPresent();
        assertThat(weather.get().getTemperature()).isEqualTo(-5.1);
    }

    @Test
    void serviceReturnError() throws URISyntaxException {
        mockServer.expect(
                        requestTo(new URI(String.format("%s/current.json?q=%s&key=%s", url, "Test", apiKey))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": {\"code\": 1006, \"message\": \"location not found\"}}")
                );

        assertThrows(WrongLocationException.class, () -> weatherApiService.getCurrentWeatherByRegion("Test"));
    }
}