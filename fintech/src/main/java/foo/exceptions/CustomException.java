package foo.exceptions;

import foo.models.WeatherErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final WeatherErrorResponse errorMessage;

    protected CustomException(HttpStatus httpStatus, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = new WeatherErrorResponse(errorMessage);
    }

    protected CustomException(HttpStatus httpStatus, String errorMessage, Throwable cause) {
        super(cause);
        this.httpStatus = httpStatus;
        this.errorMessage = new WeatherErrorResponse(errorMessage);
    }

}
