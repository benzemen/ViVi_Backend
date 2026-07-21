package com.substring.chat.chat_app_backend.repositories;

import com.substring.chat.chat_app_backend.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * MongoDB repository for {@link User} documents.
 */
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
