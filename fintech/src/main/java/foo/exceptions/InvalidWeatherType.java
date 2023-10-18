package foo.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidWeatherType extends CustomException {
    public InvalidWeatherType() {
        super(HttpStatus.BAD_REQUEST, "Invalid weather type entered");
    }
}
