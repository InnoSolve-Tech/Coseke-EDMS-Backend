package com.cosek.edms.user;

import com.cosek.edms.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/users/{id}")
    public ResponseEntity<User> findUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findOneUser(id));
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

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = userService.deleteUser(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user, id));
    }
}
