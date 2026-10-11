package com.education.platform.repository;

import com.education.platform.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByIsActiveTrue();
    
    @Query("SELECT e FROM Exam e WHERE e.createdBy.id = ?1")
    List<Exam> findByCreatedById(Long teacherId);
}