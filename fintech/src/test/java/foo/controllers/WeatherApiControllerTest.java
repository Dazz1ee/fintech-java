package foo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.exceptions.TooManyLocationsException;
import foo.services.ClientWeather;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = WeatherApiController.class, properties = "api_key= aaa")
class WeatherApiControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientWeather clientWeather;

    @Test
    void getWeatherWhenOk() throws Exception {
        when(clientWeather.getCurrentWeatherByRegion("Test")).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get(new URI(String.format("/remote/api/weather/%s", "Test")))).andExpect(status().isOk());
    }

    @Test
    void exceptionHandleTest() throws Exception {
        when(clientWeather.getCurrentWeatherByRegion("Test")).thenThrow(new TooManyLocationsException("message"));
        mockMvc.perform(get(String.format("/remote/api/weather/%s", "Test"))).andExpect(status().isBadRequest());
    }
}