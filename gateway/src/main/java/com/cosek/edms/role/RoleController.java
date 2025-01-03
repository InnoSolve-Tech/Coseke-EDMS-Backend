package com.cosek.edms.role;

import com.cosek.edms.exception.NotFoundException;
import com.cosek.edms.permission.Permission;
import com.cosek.edms.role.Models.MultipleUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping()
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role response = roleService.createRole(role);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Role> deleteRole(@PathVariable Long id) {
        Role response = roleService.deleteRole(id);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @GetMapping()
    public ResponseEntity<List<Role>> listRoles() {
        List<Role> response = roleService.listRoles();
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @PutMapping("/{roleID}/add/{permID}")
    public ResponseEntity<Role> addPermissionToRole(@PathVariable Long roleID, @PathVariable Long permID) throws NotFoundException {
        Role response = roleService.addPermissionToRole(roleID, permID);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @PutMapping("/{roleID}/remove/{permID}")
    public ResponseEntity<Role> removePermissionToRole(@PathVariable Long roleID, @PathVariable Long permID) throws NotFoundException {
        Role response = roleService.removePermissionFromRole(roleID, permID);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    @PutMapping("/{roleId}/update-permissions")
    public ResponseEntity<Role> addMultiplePermissions(@PathVariable Long roleId, @RequestBody MultipleUpdate update) {
        try {
            // Extract permission IDs from the list of permissions
            List<Long> permissionIds = update.permissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toList());

            // Pass the list of IDs to the service method
            return ResponseEntity.ok(roleService.addMultiplePermissions(roleId, update.status, permissionIds));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
