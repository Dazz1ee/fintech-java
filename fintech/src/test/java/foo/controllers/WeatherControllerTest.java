package foo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.configurations.CsrfConfiguration;
import foo.configurations.SecurityConfig;
import foo.configurations.UriBuilderConfig;
import foo.models.*;
import foo.other.CustomUriBuilder;
import foo.services.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(controllers = {WeatherController.class})
@ActiveProfiles("test")
@Import({UriBuilderConfig.class, SecurityConfig.class, CsrfConfiguration.class})
class WeatherControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Qualifier("uriBuilderForWeatherApi")
    @Autowired
    private CustomUriBuilder customUriBuilder;


    @Test
    @WithMockUser
    void getTemperatureByRegionIdThenNotFound() throws Exception {
        when(weatherService.findWeatherByRegion(any(Long.class), any(LocalDateTime.class))).thenReturn(Optional.empty());
        mockMvc.perform(
                get("/api/weather/0?date=2025-12-12T10:50")
        ).andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser
    void getTemperatureWillReturn405() throws Exception {
        when(weatherService.findWeatherByRegion(any(Long.class), any(LocalDateTime.class))).thenReturn(Optional.empty());
        mockMvc.perform(
                get("/api/weather/1ad?date=2025-12-12T10:50")
        ).andExpect(status().isMethodNotAllowed());

    }

    @Test
    @WithMockUser
    void getTemperatureByRegionIdWhenWeatherExists() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test-test";
        Weather weather = new Weather(1L, new City(regionName), new WeatherType("sunshine"), 32.1, dateTime);
        when(weatherService.findWeatherByRegion(regionName, dateTime)).thenReturn(Optional.of(weather.getTemperature()));

        mockMvc.perform(
                get("/api/weather/{regionName}?date={date}", regionName, dateTime)
        ).andExpect(status().isOk()).andExpect(content()
                .json(objectMapper.writeValueAsString(new ReturnedTemperature(weather.getTemperature()))));

    }

    @Test
    void getTemperatureWnenAnauthorized() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test-test";
        Weather weather = new Weather(1L, new City(regionName), new WeatherType("sunshine"), 32.1, dateTime);
        when(weatherService.findWeatherByRegion(regionName, dateTime)).thenReturn(Optional.of(weather.getTemperature()));

        mockMvc.perform(
                get("/api/weather/{regionName}?date={date}", regionName, dateTime)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addNewRegion() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        Long regionId = 1L;

        WeatherRequest weatherRequest = new WeatherRequest(23.1, "sunshine", dateTime);
        URI expected = customUriBuilder.getUri(regionId, dateTime);
        when(weatherService.addNewRegionWithWeather(regionName, weatherRequest)).thenReturn(expected);

        MvcResult result = mockMvc.perform(
                post("/api/weather/{regionName}", regionName)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn();

        assertThat(result.getResponse()
                .getHeader("Location"))
                .isEqualTo(expected.toString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addNewRegionWhenUserDoesntHaveRight() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        Long regionId = 1L;

        WeatherRequest weatherRequest = new WeatherRequest(23.1, "sunshine", dateTime);
        URI expected = customUriBuilder.getUri(regionId, dateTime);
        when(weatherService.addNewRegionWithWeather(regionName, weatherRequest)).thenReturn(expected);

        mockMvc.perform(
                post("/api/weather/{regionName}", regionName)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateWeatherByRegionNameShouldReturnNoContent() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        WeatherRequest weatherRequest = new WeatherRequest(23.1, "sunshine", dateTime);
        when(weatherService.updateWeatherByRegion(regionName, weatherRequest)).thenReturn(Optional.empty());

        mockMvc.perform(
                put("/api/weather/{regionName}", regionName)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateWeatherByRegionNameShouldReturnCreated() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        WeatherRequest weatherRequest = new WeatherRequest(23.1, "sunshine", dateTime);
        URI expected = customUriBuilder.getUri(0L, dateTime);
        when(weatherService.updateWeatherByRegion(regionName, weatherRequest)).thenReturn(Optional.of(expected));

        MvcResult result = mockMvc.perform(
                put("/api/weather/{regionName}", regionName)
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
        ).andExpect(status().isCreated()).andReturn();

        assertThat(result.getResponse()
                .getHeader("Location"))
                .isEqualTo(expected.toString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateWeatherByRegionNameShouldReturnCreatedWhenUserDoesntHaveRight() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String regionName = "Test";
        WeatherRequest weatherRequest = new WeatherRequest(23.1, "sunshine", dateTime);
        URI expected = customUriBuilder.getUri(0L, dateTime);
        when(weatherService.updateWeatherByRegion(regionName, weatherRequest)).thenReturn(Optional.of(expected));

        mockMvc.perform(
                put("/api/weather/{regionName}", regionName)
                        .content(objectMapper.writeValueAsString(weatherRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
        ).andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteWeatherByRegionName() throws Exception {
        String regionName = "test";
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        mockMvc.perform(
                delete("/api/weather/{regionName}", regionName).with(csrf())
        ).andExpect(status().isNoContent());

        verify(weatherService).removeWeathersByRegionName(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(regionName);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteWeatherByRegionId() throws Exception {
        Long regionId = 1L;
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        mockMvc.perform(
                delete("/api/weather/{regionId}", regionId).with(csrf())

        ).andExpect(status().isNoContent());

        verify(weatherService).removeWeathersByRegionId(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(regionId);

    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteWeatherByRegionIdWhenUserDoesntHaveRight() throws Exception {
        Long regionId = 1L;
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        mockMvc.perform(
                delete("/api/weather/{regionId}", regionId).with(csrf())

        ).andExpect(status().isForbidden());

    }

}