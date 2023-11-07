package foo.controllers;

import foo.models.UserDto;
import foo.services.IdentificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class IdentificationController {

    private final IdentificationService identificationService;

    @PostMapping("/registration")
    public ResponseEntity<Void> signUp(@RequestBody UserDto userDto) {
        identificationService.signUp(userDto);
        return ResponseEntity.ok().build();
    }

}
