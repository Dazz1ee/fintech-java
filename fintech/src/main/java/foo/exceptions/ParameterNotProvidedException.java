package foo.exceptions;

import org.springframework.http.HttpStatus;

public class ParameterNotProvidedException extends CustomException{
    public ParameterNotProvidedException(String errorMessage) {
        super(HttpStatus.BAD_REQUEST, errorMessage);
    }
}
