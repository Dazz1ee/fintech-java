package foo.exceptions;

import org.springframework.http.HttpStatus;

public class CustomSQLException extends CustomException{
    public CustomSQLException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", cause);
    }

}
