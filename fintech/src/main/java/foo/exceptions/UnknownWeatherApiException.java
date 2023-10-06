package foo.exceptions;

import org.springframework.http.HttpStatus;

public class UnknownWeatherApiException  extends CustomException{
    public UnknownWeatherApiException(String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
