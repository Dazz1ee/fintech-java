package foo.exceptions;

import org.springframework.http.HttpStatus;

public class CreatingConnectionException extends CustomException{
    public CreatingConnectionException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create connection", cause);
    }
}
