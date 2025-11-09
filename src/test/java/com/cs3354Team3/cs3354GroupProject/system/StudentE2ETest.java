package com.cs3354Team3.cs3354GroupProject.system;

import com.cs3354Team3.cs3354GroupProject.entity.*;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // everything in each test is rolled back afterwards
public class StudentE2ETest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Full flow:
     *  1. Clear existing DB state (for isolation) inside the test transaction.
     *  2. Create a teacher, student, and a "Data Structures" course.
     *  3. Log in as the student (via @WithMockUser).
     *  4. POST /student/enroll?courseId=...
     *  5. Verify redirect, flash message, and DB enrollment.
     */
    @Test
    @WithMockUser(username = "student@test.com", authorities = {"STUDENT"})
    void studentCanEnrollInCourse_endToEnd() throws Exception {
        // ---------- 1. CLEAR DB STATE FOR THIS TEST ONLY ----------
        // Order matters for FK constraints: child -> parent
        studentCourseRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // ---------- 2. SETUP DB STATE ----------

        // Teacher
        User teacher = new User();
        teacher.setEmail("teacher@test.com");
        teacher.setPassword(passwordEncoder.encode("password"));
        teacher.setRole(Role.TEACHER);
        teacher = userRepository.save(teacher);

        // Student (username must match @WithMockUser username)
        User student = new User();
        student.setEmail("student@test.com");
        student.setPassword(passwordEncoder.encode("password"));
        student.setRole(Role.STUDENT);
        student = userRepository.save(student);

        // Course
        Course course = new Course();
        course.setName("Data Structures");
        course.setDescription("Learn about lists, trees, graphs, and more.");
        course.setCredits(3);
        course.setTeacher(teacher);
        course.setDaysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        course.setStartTime(LocalTime.of(9, 0));
        course.setEndTime(LocalTime.of(10, 15));
        course = courseRepository.save(course);

        Long courseId = course.getId();

        // ---------- 3. CALL THE REAL ENDPOINT ----------

        mvc.perform(
                        post("/student/enroll")
                                .param("courseId", courseId.toString())
                )
                // We expect a redirect back to the student dashboard
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/dashboard"))
                // And the flash "success" message your controller sets:
                // redirectAttrs.addFlashAttribute("success",
                //      "Successfully enrolled in " + course.getName() + "!");
                .andExpect(flash().attribute("success",
                        "Successfully enrolled in Data Structures!"));

        // ---------- 4. VERIFY DB STATE ----------

        List<StudentCourse> enrollments = studentCourseRepository.findByStudent(student);
        assertEquals(1, enrollments.size(), "Student should have exactly one enrollment");

        StudentCourse sc = enrollments.get(0);
        assertEquals(courseId, sc.getCourse().getId(),
                "Enrollment should be for the Data Structures course");
    }
}
