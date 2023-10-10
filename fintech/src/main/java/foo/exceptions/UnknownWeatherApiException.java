package foo.exceptions;

import org.springframework.http.HttpStatus;

public class UnknownWeatherApiException  extends CustomException{
    public UnknownWeatherApiException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
    }
}
