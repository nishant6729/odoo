package com.SkillSwap.repository;

import com.SkillSwap.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing verification tokens.
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Finds a verification token by token string.
     * @param token the token string
     * @return an Optional containing the VerificationToken if found
     */
    Optional<VerificationToken> findByToken(String token);
}
