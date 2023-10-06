package foo.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final String errorMessage;

    protected CustomException(HttpStatus httpStatus, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = "Weather API error with message = " + errorMessage;
    }

}
