package foo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.configurations.UriBuilderConfig;
import foo.exceptions.TooManyLocationsException;
import foo.models.CityNameRequest;
import foo.other.CustomUriBuilder;
import foo.services.WeatherApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {WeatherApiController.class, ObjectMapper.class, UriBuilderConfig.class})
@ActiveProfiles("test")
class WeatherApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherApiService weatherApiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("uriBuilderForWeatherApi")
    private CustomUriBuilder customUriBuilder;

    @Test
    void getWeatherWhenOk() throws Exception {
        when(weatherApiService.getCurrentWeatherByRegion("Test")).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get(new URI(String.format("/remote/api/weather/%s", "Test")))).andExpect(status().isOk());
    }


    @Test
    void exceptionHandleTest() throws Exception {
        when(weatherApiService.getCurrentWeatherByRegion("Test")).thenThrow(new TooManyLocationsException("message"));
        mockMvc.perform(get(String.format("/remote/api/weather/%s", "Test"))).andExpect(status().isBadRequest());
    }

    @Test
    void saveWeather() throws Exception {
        URI uri = customUriBuilder.getUri(1L, LocalDateTime.now());
        when(weatherApiService.saveWeather("Test"))
                .thenReturn(uri);
        mockMvc.perform(post(new URI("/remote/api/weather")).content(objectMapper.writeValueAsString(new CityNameRequest("Test"))).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andExpect(header().string("Location", uri.toString()));
    }
}