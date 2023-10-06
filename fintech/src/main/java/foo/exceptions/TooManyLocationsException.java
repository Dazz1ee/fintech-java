package foo.exceptions;

import org.springframework.http.HttpStatus;

public class TooManyLocationsException extends CustomException{
    public TooManyLocationsException(String errorMessage) {
        super(HttpStatus.BAD_REQUEST, errorMessage);
    }
}