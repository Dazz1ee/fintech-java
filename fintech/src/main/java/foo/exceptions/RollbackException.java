package foo.exceptions;

import org.springframework.http.HttpStatus;

public class RollbackException extends CustomException{
    public RollbackException(Throwable exception) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Failed rollback", exception);
    }
}
