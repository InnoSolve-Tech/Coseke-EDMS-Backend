package com.cosek.edms.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> findAll() {
        List<User> response = userService.findAllUsers();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User request) {
        User response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
