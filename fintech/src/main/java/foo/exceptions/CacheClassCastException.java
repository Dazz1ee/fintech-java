package foo.exceptions;

import org.springframework.http.HttpStatus;

public class CacheClassCastException extends CustomException {
    public CacheClassCastException(Exception ex) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "...", ex);
    }
}
