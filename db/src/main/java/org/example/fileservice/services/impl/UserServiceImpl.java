package org.example.fileservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.fileservice.models.User;
import org.example.fileservice.repositories.UserRepository;
import org.example.fileservice.services.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User createOrUpdateUser(String id, String email, String name, String pictureUrl) {
        return userRepository.findByEmail(email).orElseGet(
            () -> userRepository.save(new User(id, email, name, pictureUrl))
        );
    }

    @Override
    public User updateUser(String id, String name, String pictureUrl) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(name);
        user.setPicture(pictureUrl);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findBySub(String sub) {
        return userRepository.findById(sub);
    }
}
