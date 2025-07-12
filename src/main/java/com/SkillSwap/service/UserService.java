package com.SkillSwap.service;

import com.SkillSwap.model.User;
import com.SkillSwap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String uid) {
        return userRepository.findById(Long.valueOf(uid));
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String uid) {
        userRepository.deleteById(Long.valueOf(uid));
    }
}