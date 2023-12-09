package foo.exceptions;

import org.springframework.http.HttpStatus;

public class WeatherNotFoundException extends CustomException{
    public WeatherNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Weather not found");
    }

}
