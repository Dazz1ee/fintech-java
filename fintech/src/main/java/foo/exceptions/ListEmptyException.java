package foo.exceptions;

import org.springframework.http.HttpStatus;

public class ListEmptyException extends CustomException {
    public ListEmptyException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Trying to get an element that doesn't exist");
    }
}
