package com.cosek.edms.user;

import com.cosek.edms.exception.NotFoundException;
import com.cosek.edms.role.Role;
import com.cosek.edms.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.cosek.edms.helper.Constants.FAILED_DELETION;
import static com.cosek.edms.helper.Constants.SUCCESSFUL_DELETION;

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

    public User updateUser(User request, Long id) {
        User user = userRepository.findById(id).orElse(null);
        assert user != null;
        user.setFirst_name(request.getFirst_name());
        user.setLast_name(request.getLast_name());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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

        userRepository.deleteById(id);
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return deleteResponse(false, FAILED_DELETION, id);
        }
        return deleteResponse(true, SUCCESSFUL_DELETION, id);
    }

    private Map<String, Object> deleteResponse(boolean status, String message, Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", status);
        response.put("messages", message);
        response.put("id", id);
        return response;
    }
}