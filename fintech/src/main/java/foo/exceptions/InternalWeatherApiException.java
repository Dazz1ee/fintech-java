package foo.exceptions;

import org.springframework.http.HttpStatus;

public class InternalWeatherApiException extends CustomException{
    public InternalWeatherApiException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
    }
}
