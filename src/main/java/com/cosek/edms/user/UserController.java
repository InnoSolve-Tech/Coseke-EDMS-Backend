package com.cosek.edms.user;

import com.cosek.edms.exception.NotFoundException;
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

    @PutMapping("/users/{userID}/roles/{roleID}")
    public ResponseEntity<User> addRoleToUser(@PathVariable Long userID, @PathVariable Long roleID) throws NotFoundException {
        User response = userService.addRoleToUser(userID, roleID);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
