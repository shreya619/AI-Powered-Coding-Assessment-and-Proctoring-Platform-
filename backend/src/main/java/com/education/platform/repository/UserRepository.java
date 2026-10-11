package com.education.platform.repository;

import com.education.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' ORDER BY u.totalPoints DESC")
    List<User> findTopStudentsByPoints();
    
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' ORDER BY u.averageScore DESC")
    List<User> findTopStudentsByScore();
}