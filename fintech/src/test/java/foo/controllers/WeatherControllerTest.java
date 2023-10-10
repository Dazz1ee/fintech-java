package foo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.models.ReturnedTemperature;
import foo.models.Weather;
import foo.models.WeatherRequest;
import foo.services.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(controllers = {WeatherController.class})
class WeatherControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    public URI createUri(LocalDateTime dateTime, Long regionId) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("/api/weather/{id}")
                .queryParam("date", dateTime)
                .buildAndExpand(regionId)
                .toUri();
    }

    @Test
    void getTemperatureByRegionIdThenNotFound() throws Exception {
        when(weatherService.findWeatherByRegion(any(Long.class), any(LocalDateTime.class))).thenReturn(Optional.empty());
        mockMvc.perform(
                get("/api/weather/0?date=2025-12-12T10:50")
        ).andExpect(status().isNotFound());

    }

    @Test
    void getTemperatureWillReturn405() throws Exception {
        when(weatherService.findWeatherByRegion(any(Long.class), any(LocalDateTime.class))).thenReturn(Optional.empty());
        mockMvc.perform(
                get("/api/weather/1ad?date=2025-12-12T10:50")
        ).andExpect(status().isMethodNotAllowed());

    }

    @Test
    void getTemperatureByRegionIdWhenWeatherExists() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test-test";
        Weather weather = new Weather(1L, regionName, 32.1, dateTime);
        when(weatherService.findWeatherByRegion(regionName, dateTime)).thenReturn(Optional.of(weather.getTemperature()));

        mockMvc.perform(
                get("/api/weather/{regionName}?date={date}", regionName, dateTime)
        ).andExpect(status().isOk()).andExpect(content()
                .json(objectMapper.writeValueAsString(new ReturnedTemperature(weather.getTemperature()))));

    }

    @Test
    void addNewRegion() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        Long regionId = 1L;

        WeatherRequest weatherRequest = new WeatherRequest(23.1, dateTime);
        URI expected = createUri(dateTime, regionId);
        when(weatherService.addNewRegionWithWeather(regionName, weatherRequest)).thenReturn(expected);

        MvcResult result = mockMvc.perform(
                post("/api/weather/{regionName}", regionName)
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn();

        assertThat(result.getResponse()
                .getHeader("Location"))
                .isEqualTo(expected.toString());
    }

    @Test
    void updateWeatherByRegionNameShouldReturnNoContent() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        WeatherRequest weatherRequest = new WeatherRequest(23.1, dateTime);
        when(weatherService.updateWeatherByRegion(regionName, weatherRequest)).thenReturn(Optional.empty());

        mockMvc.perform(
                put("/api/weather/{regionName}", regionName)
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    @Test
    void updateWeatherByRegionNameShouldReturnCreated() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        WeatherRequest weatherRequest = new WeatherRequest(23.1, dateTime);
        URI expected = createUri(dateTime, 0L);
        when(weatherService.updateWeatherByRegion(regionName, weatherRequest)).thenReturn(Optional.of(expected));

        MvcResult result = mockMvc.perform(
                put("/api/weather/{regionName}", regionName)
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn();

        assertThat(result.getResponse()
                .getHeader("Location"))
                .isEqualTo(expected.toString());
    }

    @Test
    void deleteWeatherByRegionName() throws Exception {
        String regionName = "test";
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        mockMvc.perform(
                delete("/api/weather/{regionName}", regionName)
        ).andExpect(status().isNoContent());

        verify(weatherService).removeWeathersByRegionName(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(regionName);
    }

    @Test
    void deleteWeatherByRegionId() throws Exception {
        Long regionId = 1L;
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        mockMvc.perform(
                delete("/api/weather/{regionId}", regionId)
        ).andExpect(status().isNoContent());

        verify(weatherService).removeWeathersByRegionId(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(regionId);

    }

}