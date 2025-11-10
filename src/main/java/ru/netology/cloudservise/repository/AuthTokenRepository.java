package ru.netology.cloudservise.repository;

import ru.netology.cloudservise.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("SELECT t FROM AuthToken t WHERE t.expiresAt > :now")
    List<AuthToken> findAllActiveTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}