package foo.fintech.contollers;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.fintech.models.Weather;
import foo.fintech.models.WeatherRequest;
import foo.fintech.services.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest
class WeatherControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void getTemperatureByRegionIdThenNotFound() throws Exception {
        when(weatherService.findWeatherByRegion(any(), any(), any())).thenReturn(Optional.empty());
        mockMvc.perform(
                get("/api/weather/0?date=05:05 10/12/2023")
        ).andExpect(status().isNotFound());

    }

    @Test
    void getTemperatureByRegionIdWhenWeatherExists() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        Weather weather = new Weather(1L, regionName, 32.1, dateTime);
        when(weatherService.findWeatherByRegion(eq(regionName), eq(dateTime), any())).thenReturn(Optional.of(weather));

        mockMvc.perform(
                get("/api/weather/{regionName}?date={date}", regionName, dateTime)
        ).andExpect(status().isOk()).andExpect(content().json(objectMapper.writeValueAsString(weather.getTemperature())));

    }

    @Test
    void addNewRegion() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        Long expected = 1L;
        WeatherRequest weatherRequest = new WeatherRequest(23.1, dateTime);
        when(weatherService.addNewRegionWithWeather(regionName, weatherRequest)).thenReturn(expected);

        MvcResult result = mockMvc.perform(
                post("/api/weather/{regionName}", regionName)
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn();

        assertThat(result.getResponse().getHeader("Location")).isEqualTo("http://localhost:8080/api/weather/1?date=" + dateTime.toString());
    }

    @Test
    void updateWeatherByRegionNameShouldReturnNoContent() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        Long expected = 1L;
        WeatherRequest weatherRequest = new WeatherRequest(23.1, dateTime);
        when(weatherService.updateWeatherByRegion(regionName, weatherRequest)).thenReturn(-1L);

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
        when(weatherService.updateWeatherByRegion(regionName, weatherRequest)).thenReturn(0L);

        MvcResult result = mockMvc.perform(
                put("/api/weather/{regionName}", regionName)
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn();

        assertThat(result.getResponse().getHeader("Location")).isEqualTo("http://localhost:8080/api/weather/0?date=" + dateTime.toString());
    }

    @Test
    void deleteWeatherByRegionNameIfDeleted() throws Exception {
        String regionName = "test";
        when(weatherService.removeWeathersByParameter(eq(regionName), any())).thenReturn(true);

        mockMvc.perform(
                delete("/api/weather/{regionName}", regionName)
        ).andExpect(status().isNoContent());
    }

    @Test
    void deleteWeatherByRegionNameIfNotDeleted() throws Exception {
        String regionName = "Test";
        when(weatherService.removeWeathersByParameter(eq(regionName), any())).thenReturn(false);

        mockMvc.perform(
                delete("/api/weather/{regionName}", regionName)
        ).andExpect(status().isNotFound());
    }

    @Test
    void deleteWeatherByRegionIdIfDeleted() throws Exception {
        Long regionId = 1L;
        when(weatherService.removeWeathersByParameter(eq(regionId), any())).thenReturn(true);

        mockMvc.perform(
                delete("/api/weather/{regionId}", regionId)
        ).andExpect(status().isNoContent());
    }

    @Test
    void deleteWeatherByRegionIdIfNotDeleted() throws Exception {
        Long regionId = 1L;
        when(weatherService.removeWeathersByParameter(eq(regionId), any())).thenReturn(false);

        mockMvc.perform(
                delete("/api/weather/{regionId}", regionId)
        ).andExpect(status().isNotFound());
    }
}