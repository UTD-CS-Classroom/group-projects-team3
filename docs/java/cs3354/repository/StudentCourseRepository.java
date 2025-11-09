package com.cs3354Team3.cs3354GroupProject.repository;

import com.cs3354Team3.cs3354GroupProject.entity.StudentCourse;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByStudent(User student);
    List<StudentCourse> findByCourse(Course course);
}