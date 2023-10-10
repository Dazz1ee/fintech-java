package foo.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidWeatherApiKeyException extends CustomException{
    public InvalidWeatherApiKeyException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Ooops, service is not available");
    }
}