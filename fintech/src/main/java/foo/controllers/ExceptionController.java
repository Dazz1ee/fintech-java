package foo.controllers;

import foo.exceptions.*;
import foo.models.WeatherErrorResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<WeatherErrorResponse> unknownWeatherError(CustomException weatherApiException) {
        return ResponseEntity.status(weatherApiException.getHttpStatus()).body(weatherApiException.getErrorMessage());
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<WeatherErrorResponse> locationNotFound() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new WeatherErrorResponse("Try later"));
    }

    @ExceptionHandler({HttpClientErrorException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<WeatherErrorResponse> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new WeatherErrorResponse("Enters not correct data"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WeatherErrorResponse> unknownException(Exception exception) {
        log.error("error message: \n {} \n {}", exception.getMessage());
        return ResponseEntity.internalServerError().body(new WeatherErrorResponse("Пу-пу-пу"));
    }

}
