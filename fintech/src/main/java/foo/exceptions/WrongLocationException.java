package foo.exceptions;

import org.springframework.http.HttpStatus;

public class WrongLocationException extends CustomException{
    public WrongLocationException(String errorMessage) {
        super(HttpStatus.BAD_REQUEST, String.format("Location not found. %s", errorMessage));
    }
}
