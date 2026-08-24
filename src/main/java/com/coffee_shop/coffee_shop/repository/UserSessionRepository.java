package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    List<UserSession> findByUserIdAndRevokedFalse(Long userId);

    List<UserSession> findAllByUserIdAndRevokedFalse(Long userId);

    Optional<UserSession> findBySessionIdAndRevokedFalse(String sessionId);
}