package com.cosek.edms.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(User request) {
        return userRepository.save(request);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}