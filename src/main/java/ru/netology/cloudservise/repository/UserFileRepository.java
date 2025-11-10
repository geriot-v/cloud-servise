package ru.netology.cloudservise.repository;

import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.entity.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    List<UserFile> findByUserOrderByUploadedAtDesc(User user);
    Optional<UserFile> findByUserAndFilename(User user, String filename);
    boolean existsByUserAndFilename(User user, String filename);

    @Modifying
    @Query("DELETE FROM UserFile f WHERE f.user = :user AND f.filename = :filename")
    void deleteByUserAndFilename(@Param("user") User user, @Param("filename") String filename);
}