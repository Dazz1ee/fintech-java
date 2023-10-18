package foo.exceptions;

import org.springframework.http.HttpStatus;

public class CreateWeatherException extends CustomException{
    public CreateWeatherException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Can't save the weather");
    }
}
