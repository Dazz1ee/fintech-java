package foo.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidUrlException extends CustomException{
    public InvalidUrlException(String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
