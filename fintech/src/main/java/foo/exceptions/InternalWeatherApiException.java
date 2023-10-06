package foo.exceptions;

import org.springframework.http.HttpStatus;

public class InternalWeatherApiException extends CustomException{
    public InternalWeatherApiException(String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
