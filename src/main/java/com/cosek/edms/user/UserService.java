package com.cosek.edms.user;

import com.cosek.edms.exception.NotFoundException;
import com.cosek.edms.role.Role;
import com.cosek.edms.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final BCryptPasswordEncoder passwordEncoder;

    public User createUser(User request) {
        User user = User
                .builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .first_name(request.getFirst_name())
                .last_name(request.getLast_name())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>())
                .build();
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findOneUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User addRoleToUser(Long userID, Long roleId) throws NotFoundException {
        User user = findOneUser(userID);
        Role role = roleService.findOneRole(roleId);
        Set<Role> roles = user.getRoles();
        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public Map<String, Object> deleteUser(Long id) {
        Map<String, Object> response = new HashMap<>();

        userRepository.deleteById(id);
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            response.put("success", false);
            response.put("messages", "Delete Failed");
            response.put("id", id);
        } else {
            response.put("success", true);
            response.put("messages", "Delete Successful");
            response.put("id", id);
        }
        return response;
    }
}