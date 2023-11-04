package foo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.configurations.CsrfConfiguration;
import foo.configurations.SecurityConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.net.URI;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {WeatherApiController.class})
@Import({SecurityConfig.class, ObjectMapper.class, UriBuilderConfig.class, CsrfConfiguration.class})
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
    @WithMockUser(roles = "USER")
    void getWeatherWhenOk() throws Exception {
        when(weatherApiService.getCurrentWeatherByRegion("Test")).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get(new URI(String.format("/remote/api/weather/%s", "Test")))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWeatherWhenOkWhenUserIsAdmin() throws Exception {
        when(weatherApiService.getCurrentWeatherByRegion("Test")).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get(new URI(String.format("/remote/api/weather/%s", "Test")))).andExpect(status().isOk());
    }

    @Test
    void notAuthorizedGetRequest() throws Exception {
        when(weatherApiService.getCurrentWeatherByRegion("Test")).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get(new URI(String.format("/remote/api/weather/%s", "Test")))).andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = "USER")
    void exceptionHandleTest() throws Exception {
        when(weatherApiService.getCurrentWeatherByRegion("Test")).thenThrow(new TooManyLocationsException("message"));
        mockMvc.perform(get(String.format("/remote/api/weather/%s", "Test"))).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test", password = "test", roles = "ADMIN")
    void saveWeather() throws Exception {
        URI uri = customUriBuilder.getUri(1L, LocalDateTime.now());
        when(weatherApiService.saveWeather("Test"))
                .thenReturn(uri);
        mockMvc.perform(post(new URI("/remote/api/weather")).with(csrf()).content(objectMapper.writeValueAsString(new CityNameRequest("Test"))).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andExpect(header().string("Location", uri.toString()));
    }

    @Test
    @WithMockUser(username = "test", password = "test", roles = "ADMIN")
    void saveWeatherWhenCsrfTokenIsIncorrect() throws Exception {
        URI uri = customUriBuilder.getUri(1L, LocalDateTime.now());
        when(weatherApiService.saveWeather("Test"))
                .thenReturn(uri);
        mockMvc.perform(post(new URI("/remote/api/weather")).with(csrf().useInvalidToken()).content(objectMapper.writeValueAsString(new CityNameRequest("Test"))).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void saveWeatherWhenUserDoesntHaveRights() throws Exception {
        URI uri = customUriBuilder.getUri(1L, LocalDateTime.now());
        when(weatherApiService.saveWeather("Test"))
                .thenReturn(uri);
        mockMvc.perform(post(new URI("/remote/api/weather")).with(csrf()).content(objectMapper.writeValueAsString(new CityNameRequest("Test"))).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }
}