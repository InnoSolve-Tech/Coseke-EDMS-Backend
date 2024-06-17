package com.cosek.edms.role;

import com.cosek.edms.exception.NotFoundException;
import com.cosek.edms.permission.Permission;
import com.cosek.edms.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("User with ID " + roleId + " is not found"));
    }

    public Role addPermissionToRole(Long roleID, Long permID) throws NotFoundException {
        Role role = findOneRole(roleID);

        Permission permission = permissionService.findOnePermission(permID);
        Set<Permission> permissions = role.getPermissions();
        permissions.add(permission);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    public List<Role> listRoles() {
        return roleRepository.findAll();
    }
}
