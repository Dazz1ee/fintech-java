package foo.exceptions;

import org.springframework.http.HttpStatus;

public class KeyNotProvidedException extends CustomException{
    public KeyNotProvidedException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Ooops, service is not available");
    }
}
