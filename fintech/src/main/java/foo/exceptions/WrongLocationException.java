package foo.exceptions;

import org.springframework.http.HttpStatus;

public class WrongLocationException extends CustomException{
    public WrongLocationException(String errorMessage) {
        super(HttpStatus.BAD_REQUEST, errorMessage);
    }
}
