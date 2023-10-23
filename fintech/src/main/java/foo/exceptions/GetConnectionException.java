package foo.exceptions;

import org.springframework.http.HttpStatus;

public class GetConnectionException extends CustomException {
    public GetConnectionException(Throwable exception) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Failed get connection from pool", exception);
    }
}
