package foo.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidWeatherApiKeyException extends CustomException{
    public InvalidWeatherApiKeyException(String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}