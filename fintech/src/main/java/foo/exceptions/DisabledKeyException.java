package foo.exceptions;

import org.springframework.http.HttpStatus;

public class DisabledKeyException extends CustomException{
    public DisabledKeyException(String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
