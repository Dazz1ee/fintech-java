package foo.exceptions;

import org.springframework.http.HttpStatus;

public class UpdateWeatherException extends CustomException {
    public UpdateWeatherException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update data");
    }
}
