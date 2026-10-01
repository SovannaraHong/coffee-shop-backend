package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.IpLoginAttempt;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IpLoginAttemptRepository extends JpaRepository<IpLoginAttempt, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from IpLoginAttempt i where i.ipAddress = :ip")
    Optional<IpLoginAttempt> findByIdForUpdate(@Param("ip") String ip);

}