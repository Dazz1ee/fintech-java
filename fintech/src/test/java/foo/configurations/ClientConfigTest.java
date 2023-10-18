package foo.configurations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foo.exceptions.WrongLocationException;
import foo.models.WeatherApiResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(properties = "api_key: aaa")
class ClientConfigTest {
    @Autowired
    @Qualifier("weatherTemplate")
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper;

    @BeforeAll
    public void init() {
        mapper = new ObjectMapper();
    }

    @BeforeEach
    public void initServer() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }


    @Value("${api_url}")
    private String url;



    @Test
    void checkRestTemplateInterceptors() throws JsonProcessingException, URISyntaxException {
        WeatherApiResponse weatherApiResponse = new WeatherApiResponse(null, null);

        mockServer.expect(
                requestTo(new URI(String.format("%s/current.json?q=%s&key=%s", url, "London", "aaa"))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(weatherApiResponse))
                );

        ResponseEntity<WeatherApiResponse> entity = restTemplate.exchange("/current.json?q={q}", HttpMethod.GET, null, WeatherApiResponse.class, "London");
        assertThat(entity.getBody()).isEqualTo(weatherApiResponse);
    }
    @Test
    void handleError() throws JsonProcessingException, URISyntaxException {
        Map<String, Map<String, String>> response = new HashMap<>();
        response.put("error", new HashMap<>());
        response.get("error").put("message",  "error");
        response.get("error").put("code", String.valueOf(1006));

        mockServer.expect(
                        requestTo(new URI(String.format("%s/current.json?q=%s&key=%s", url, "London", "aaa"))))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(mapper.writeValueAsString(response))
                );

        assertThrows(WrongLocationException.class, () ->
            restTemplate.exchange("/current.json?q={q}", HttpMethod.GET, null, WeatherApiResponse.class, "London")
        );
    }

}