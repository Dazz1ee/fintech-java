package foo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler({HttpClientErrorException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<?> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Введены некорректные данные");
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unknownException() {
        return ResponseEntity.internalServerError().body("Пу-пу-пу");
    }

}
