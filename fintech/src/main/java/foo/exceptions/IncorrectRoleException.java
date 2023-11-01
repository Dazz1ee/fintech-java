package foo.exceptions;

import org.springframework.http.HttpStatus;

public class IncorrectRoleException extends CustomException {
    public IncorrectRoleException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "server error");
    }
}
