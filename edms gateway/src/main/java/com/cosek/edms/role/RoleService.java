package com.cosek.edms.role;

import com.cosek.edms.exception.NotFoundException;
import com.cosek.edms.permission.Permission;
import com.cosek.edms.permission.PermissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;

    public Role createRole(Role request) {
        return roleRepository.save(request);
    }

    public Role findOneRole(Long roleId) throws NotFoundException {
        return roleRepository.findById(roleId).orElse(null);
    }

    public Role addPermissionToRole(Long roleID, Long permID) throws NotFoundException {
        Role role = findOneRole(roleID);

        Permission permission = permissionService.findOnePermission(permID);
        Set<Permission> permissions = role.getPermissions();
        permissions.add(permission);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    public Role removePermissionFromRole(Long roleId, Long permId) throws NotFoundException {
        // Find the role by its ID
        Role role = findOneRole(roleId);

        // Check if the role exists
        if (role == null) {
            throw new NotFoundException("Role not found with ID: " + roleId);
        }

        // Find the permission by its ID
        Permission permission = permissionService.findOnePermission(permId);

        // Check if the permission exists
        if (permission == null) {
            throw new NotFoundException("Permission not found with ID: " + permId);
        }

        // Remove the permission from the role
        Set<Permission> permissions = role.getPermissions();
        if (permissions.contains(permission)) {
            permissions.remove(permission);
            role.setPermissions(permissions);
        }

        // Save the role with updated permissions
        return roleRepository.save(role);
    }


    @Transactional
    public Role addMultiplePermissions(Long roleId, boolean status, List<Long> permissionIds) throws NotFoundException {
        // Find the role by its ID
        Role role = findOneRole(roleId);

        // Check if the role exists
        if (role == null) {
            throw new NotFoundException("Role not found with ID: " + roleId);
        }

        // Fetch permissions from the database
        List<Permission> permissions = new ArrayList<>();
        for (Long permissionId : permissionIds) {
            Permission permission = permissionService.findOnePermission(permissionId);
            if (permission != null) {
                permissions.add(permission);
            } else {
                throw new NotFoundException("Permission not found with ID: " + permissionId);
            }
        }

        // Handle adding permissions
        if (status) {
            for (Permission permission : permissions) {
                // Add permission only if it's not already assigned to the role
                if (!role.getPermissions().contains(permission)) {
                    role.getPermissions().add(permission);
                }
            }
        } else {
            // Handle removing permissions
            for (Permission permission : permissions) {
                // Remove permission only if it exists in the role
                role.getPermissions().remove(permission);
            }
        }

        // Save the role with updated permissions
        return roleRepository.save(role);
    }

    public List<Role> listRoles() {
        return roleRepository.findAll();
    }
}
