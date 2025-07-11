package com.cosek.edms.config;

import com.cosek.edms.permission.Permission;
import com.cosek.edms.permission.PermissionRepository;
import com.cosek.edms.role.Role;
import com.cosek.edms.role.RoleRepository;
import com.cosek.edms.user.User;
import com.cosek.edms.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.cosek.edms.helper.Constants.*;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            List<Permission> permissions = initializePermissions();
            Role adminRole = initializeAdminRole(permissions);
            initializeAdminUser(adminRole);
        };
    }

    private List<Permission> initializePermissions() {
        return Arrays.asList(
                // Permission permissions
                ensurePermission(READ_PERMISSION),
                ensurePermission(CREATE_PERMISSION),
                ensurePermission(UPDATE_PERMISSION),
                ensurePermission(DELETE_PERMISSION),
                // Role permissions
                ensurePermission(READ_ROLE),
                ensurePermission(CREATE_ROLE),
                ensurePermission(UPDATE_ROLE),
                ensurePermission(DELETE_ROLE),
                // User permissions
                ensurePermission(CREATE_USER),
                ensurePermission(READ_USER),
                ensurePermission(UPDATE_USER),
                ensurePermission(DELETE_USER),
                // Workflow permissions
                ensurePermission(READ_WORKFLOW),
                ensurePermission(CREATE_WORKFLOW),
                ensurePermission(UPDATE_WORKFLOW),
                ensurePermission(DELETE_WORKFLOW),
                // Form permissions
                ensurePermission(READ_FORM),
                ensurePermission(CREATE_FORM),
                ensurePermission(UPDATE_FORM),
                ensurePermission(DELETE_FORM),
                // Log permissions
                ensurePermission(READ_LOG),
                ensurePermission(CREATE_LOG),
                ensurePermission(UPDATE_LOG),
                ensurePermission(DELETE_LOG),
                // Folder permissions
                ensurePermission(CREATE_FOLDER),
                ensurePermission(UPDATE_FOLDER),
                ensurePermission(DELETE_FOLDER)
        );
    }

    private Permission ensurePermission(String permissionName) {
        return permissionRepository.findByName(permissionName)
                .orElseGet(() -> permissionRepository.save(new Permission(null, permissionName, new HashSet<>())));
    }

    private Role initializeAdminRole(List<Permission> permissions) {
        return roleRepository.findByName(SUPER_ADMIN)
                .orElseGet(() -> roleRepository.save(
                        new Role(null, SUPER_ADMIN, null, new HashSet<>(permissions))
                ));
    }

    private void initializeAdminUser(Role adminRole) {
        HashSet<Role> roles = new HashSet<>();
        roles.add(adminRole);
        userRepository.findByEmail(ADMIN_EMAIL).orElseGet(() -> {
            User admin = User.builder()
                    .first_name(ADMIN_FIRST_NAME)
                    .last_name(ADMIN_LAST_NAME)
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .phone(ADMIN_PHONE)
                    .address(ADMIN_COUNTRY)
                    .roles(roles)
                    .build();
            return userRepository.save(admin);
        });
    }
}