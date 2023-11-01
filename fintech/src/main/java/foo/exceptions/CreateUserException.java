package foo.exceptions;

import org.springframework.http.HttpStatus;

public class CreateUserException extends CustomException {
    public CreateUserException(Exception e) {
        super(HttpStatus.BAD_REQUEST, "Login already exists", e);
    }

    public CreateUserException() {
        super(HttpStatus.BAD_REQUEST, "Login already exists");
    }

    public CreateUserException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

}
