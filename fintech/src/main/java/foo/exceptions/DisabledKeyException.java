package foo.exceptions;

import org.springframework.http.HttpStatus;

public class DisabledKeyException extends CustomException{
    public DisabledKeyException() {

        super(HttpStatus.INTERNAL_SERVER_ERROR, "Ooops, service is not available");
    }
}
