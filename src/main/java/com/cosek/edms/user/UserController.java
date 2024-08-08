package com.cosek.edms.user;

import com.cosek.edms.exception.NotFoundException;
import com.cosek.edms.role.Role;
import com.cosek.edms.user.Models.CreateUserRequest;
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
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User response = null;
        try {
            response = userService.createUser(request);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
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
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody CreateUserRequest user) {
        try {
            return ResponseEntity.ok(userService.updateUser(user, id));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/users/roles-update/{id}")
    public  ResponseEntity<User> updateRoles(@PathVariable Long id, @RequestBody List<Role> roles) {
        try {
            return ResponseEntity.ok(userService.updateRoles(id, roles));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
