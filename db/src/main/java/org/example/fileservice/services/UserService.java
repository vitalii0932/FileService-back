package org.example.fileservice.services;

import org.example.fileservice.models.User;

import java.util.Optional;

public interface UserService {
    User createOrUpdateUser(String id, String email, String name, String picture);
    User updateUser(String id, String name, String pictureUrl);
    Optional<User> findBySub(String sub);
}
