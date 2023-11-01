package foo.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
