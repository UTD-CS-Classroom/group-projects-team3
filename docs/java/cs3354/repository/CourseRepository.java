package com.cs3354Team3.cs3354GroupProject.repository;

import com.cs3354Team3.cs3354GroupProject.entity.Course;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacher(User teacher);
}