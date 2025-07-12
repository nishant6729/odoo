package com.SkillSwap.repository;

import com.SkillSwap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email.
     * @param email the user's email address
     * @return an Optional containing the user if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);
}
