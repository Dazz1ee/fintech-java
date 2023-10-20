package foo.exceptions;

import org.springframework.http.HttpStatus;

public class UnknownSQLException extends CustomException{
    public UnknownSQLException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", cause);
    }

}
