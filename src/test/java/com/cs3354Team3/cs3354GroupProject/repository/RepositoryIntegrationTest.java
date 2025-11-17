package com.cs3354Team3.cs3354GroupProject.repository;

import com.cs3354Team3.cs3354GroupProject.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RepositoryIntegrationTest {

    // Provide missing BCryptPasswordEncoder bean for the test context
    @TestConfiguration
    static class TestConfig {
        @Bean
        public BCryptPasswordEncoder bCryptPasswordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private StudentCourseRepository studentCourseRepo;

    /**
     * Requirement: A professor can submit/own a course.
     * This test validates the @ManyToOne relationship on Course.teacher
     * and the custom `findByTeacher` method.
     * Links to: FR #2, FR #3
     */
    @Test
    void testCourseRepository_FindByTeacher() {
        // --- 1. ARRANGE ---
        User teacher1 = new User(null, "teacher1@test.com", "pass", Role.TEACHER);
        User teacher2 = new User(null, "teacher2@test.com", "pass", Role.TEACHER);
        entityManager.persist(teacher1);
        entityManager.persist(teacher2);

        Course course1 = new Course(
                null,
                "Course A",
                "...",
                3,
                teacher1,
                Set.of(DayOfWeek.MONDAY),
                LocalTime.NOON,
                LocalTime.MIDNIGHT,
                null,
                null
        );

        Course course2 = new Course(
                null,
                "Course B",
                "...",
                3,
                teacher2,
                Set.of(DayOfWeek.TUESDAY),
                LocalTime.NOON,
                LocalTime.MIDNIGHT,
                null,
                null
        );

        Course course3 = new Course(
                null,
                "Course C",
                "...",
                3,
                teacher1,
                Set.of(DayOfWeek.FRIDAY),
                LocalTime.NOON,
                LocalTime.MIDNIGHT,
                null,
                null
        );

        entityManager.persist(course1);
        entityManager.persist(course2);
        entityManager.persist(course3);
        entityManager.flush();

        // --- 2. ACT ---
        List<Course> teacher1Courses = courseRepo.findByTeacher(teacher1);

        // --- 3. ASSERT ---
        assertThat(teacher1Courses).hasSize(2);
        assertThat(teacher1Courses).contains(course1, course3);
        assertThat(teacher1Courses).doesNotContain(course2);
    }

    /**
     * Requirement: A student can be enrolled in courses, and the system can find them.
     * This test validates the @ManyToOne relationships in StudentCourse.
     * Links to: FR #4, FR #5
     */
    @Test
    void testStudentCourseRepository_FindByStudent() {
        // --- 1. ARRANGE ---
        User student1 = new User(null, "student1@test.com", "pass", Role.STUDENT);
        User student2 = new User(null, "student2@test.com", "pass", Role.STUDENT);
        entityManager.persist(student1);
        entityManager.persist(student2);

        Course course = new Course(
                null,
                "Course A",
                "...",
                3,
                null,
                Set.of(DayOfWeek.MONDAY),
                LocalTime.NOON,
                LocalTime.MIDNIGHT,
                null,
                null
        );
        entityManager.persist(course);

        // Enroll student1 in the course
        StudentCourse enrollment = new StudentCourse(null, student1, course);
        entityManager.persist(enrollment);
        entityManager.flush();

        // --- 2. ACT ---
        List<StudentCourse> student1Enrollments = studentCourseRepo.findByStudent(student1);
        List<StudentCourse> student2Enrollments = studentCourseRepo.findByStudent(student2);

        // --- 3. ASSERT ---
        assertThat(student1Enrollments).hasSize(1);
        assertThat(student1Enrollments).contains(enrollment);
        assertThat(student2Enrollments).isEmpty();
    }

    /**
     * Requirement: A course saves all its details, including meeting days.
     * This test validates the @ElementCollection for `daysOfWeek` in Course.java
     * Links to: FR #3
     */
    @Test
    void testCourse_SavesDaysOfWeekCorrectly() {
        // --- 1. ARRANGE ---
        Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

        Course course = new Course(
                null,
                "MWF Class",
                "...",
                3,
                null,
                days,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                null,
                null
        );

        // --- 2. ACT ---
        entityManager.persist(course);
        entityManager.flush();
        entityManager.clear(); // Clear the cache to force a real DB read

        Course found = courseRepo.findById(course.getId()).orElseThrow();

        // --- 3. ASSERT ---
        assertThat(found.getName()).isEqualTo("MWF Class");
        assertThat(found.getDaysOfWeek()).hasSize(3);
        assertThat(found.getDaysOfWeek())
                .contains(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    }
}
