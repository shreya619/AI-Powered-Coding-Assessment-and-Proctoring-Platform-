package com.education.platform.repository;

import com.education.platform.model.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByUserId(Long userId);
    
    List<ExamResult> findByUserIdAndExamId(Long userId, Long examId);
    
    Optional<ExamResult> findByUserIdAndExamIdAndIsCompleted(Long userId, Long examId, Boolean isCompleted);
    
    @Query("SELECT AVG(er.score) FROM ExamResult er WHERE er.user.id = ?1 AND er.isCompleted = true")
    Double findAverageScoreByUserId(Long userId);
}