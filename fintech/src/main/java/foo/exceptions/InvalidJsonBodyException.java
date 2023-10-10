package foo.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidJsonBodyException extends CustomException{
    public InvalidJsonBodyException(String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
