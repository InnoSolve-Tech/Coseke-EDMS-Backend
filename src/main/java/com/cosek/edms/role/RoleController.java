package com.cosek.edms.role;

import com.cosek.edms.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role response = roleService.createRole(role);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @PutMapping("roles/{roleID}/permissions/{permID}")
    public ResponseEntity<Role> addPermissionToRole(@PathVariable Long roleID, @PathVariable Long permID) throws NotFoundException {
        Role response = roleService.addPermissionToRole(roleID, permID);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

}
