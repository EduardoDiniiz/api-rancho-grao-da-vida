package com.rancho.api.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.login) = LOWER(:identifier)
               OR LOWER(u.email) = LOWER(:identifier)
            """)
    Optional<User> findByLoginOrEmail(@Param("identifier") String identifier);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL
               OR LOWER(u.name) LIKE :search
               OR LOWER(u.login) LIKE :search
               OR LOWER(u.email) LIKE :search)
            """)
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
}
