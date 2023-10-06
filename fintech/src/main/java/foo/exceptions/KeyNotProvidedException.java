package foo.exceptions;

import org.springframework.http.HttpStatus;

public class KeyNotProvidedException extends CustomException{
    public KeyNotProvidedException(String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
