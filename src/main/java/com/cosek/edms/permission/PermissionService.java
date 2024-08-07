package com.cosek.edms.permission;

import com.cosek.edms.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PermissionService {
    @Autowired
    public PermissionRepository permissionRepository;

    public Permission createPermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    public Permission findOnePermission(Long permID) throws NotFoundException {
        return permissionRepository.findById(permID)
                .orElseThrow(() -> new NotFoundException("Permission with ID: " + permID + " not found"));
    }

    public Permission updatePermission(Long permID, Permission request) throws NotFoundException {
        Permission permission = findOnePermission(permID);
        permission.setName(request.getName());
        return permissionRepository.save(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
}
