package foo.exceptions;

import org.springframework.http.HttpStatus;

public class NotAllowedQueryException extends CustomException{
    public NotAllowedQueryException(String errorMessage) {
        super(HttpStatus.FORBIDDEN, errorMessage);
    }
}
