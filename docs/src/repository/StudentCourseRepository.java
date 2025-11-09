package repository;

import entity.StudentCourse;
import entity.User;
import entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByStudent(User student);
    List<StudentCourse> findByCourse(Course course);
}