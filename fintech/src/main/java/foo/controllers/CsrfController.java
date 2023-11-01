package foo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {
    @GetMapping("csrf/token")
    public ResponseEntity<?> getToken(CsrfToken csrfToken) {
        return ResponseEntity.ok(csrfToken);
    }
}
