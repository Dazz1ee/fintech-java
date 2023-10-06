package foo.exceptions;

import org.springframework.http.HttpStatus;

public class ExceededCallsException extends CustomException{
    public ExceededCallsException(String errorMessage) {

        super(HttpStatus.SERVICE_UNAVAILABLE, errorMessage);
    }
}
