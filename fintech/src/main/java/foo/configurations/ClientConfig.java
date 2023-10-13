package foo.configurations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import foo.exceptions.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.net.URI;

@Configuration
@Data
public class ClientConfig {

    @Value("${api_url}")
    private String apiUrl;

    @Value("${api_key}")
    private String apiKey;

    private  final ObjectMapper objectMapper;

    @Bean
    public RestTemplate weatherTemplate() {
        return new RestTemplateBuilder().rootUri(apiUrl).additionalInterceptors((request,body, execution) -> {
                HttpRequest httpRequest = new HttpRequestWrapper(request) {
                    @Override
                    public URI getURI() {
                        return UriComponentsBuilder.fromUri(super.getURI())
                                .queryParam("key", apiKey).build().toUri();
                    }
                };
                return execution.execute(httpRequest, body);
        }).errorHandler(currentWeatherErrorHandler()).build();
    }

    @Bean
    public ResponseErrorHandler currentWeatherErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                HttpStatusCode statusCode = response.getStatusCode();
                JsonNode error = objectMapper.readTree(response.getBody()).get("error");
                String message = error.get("message").asText();
                if (statusCode.is5xxServerError()) {
                    throw new UnknownWeatherApiException();
                }

                int errorCode = error.get("code").asInt();

                switch (errorCode) {
                    case 1002 -> throw new KeyNotProvidedException();
                    case 1003 -> throw new ParameterNotProvidedException(message);
                    case 1005 -> throw new InvalidUrlException(message);
                    case 1006 -> throw new WrongLocationException(message);
                    case 2006 -> throw new InvalidWeatherApiKeyException();
                    case 2007 -> throw new ExceededCallsException(message);
                    case 2008 -> throw new DisabledKeyException();
                    case 2009 -> throw new NotAllowedQueryException(message);
                    case 9000 -> throw new InvalidJsonBodyException(message);
                    case 9001 -> throw new TooManyLocationsException(message);
                    case 9999 -> throw new InternalWeatherApiException();
                    default -> throw new UnknownWeatherApiException();
                }
            }
        };
    }
}
