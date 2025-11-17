package com.cs3354Team3.cs3354GroupProject.controller;

// --- Your project's classes ---
import com.cs3354Team3.cs3354GroupProject.controller.StudentController;
import com.cs3354Team3.cs3354GroupProject.entity.Course;
import com.cs3354Team3.cs3354GroupProject.entity.DayOfWeek;
import com.cs3354Team3.cs3354GroupProject.entity.Role;
import com.cs3354Team3.cs3354GroupProject.entity.StudentCourse;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;

// --- JUnit 5 and Mockito ---
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // <-- Import ArgumentCaptor
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// --- Spring Framework ---
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model; // <-- Import Model
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// --- Standard Java ---
import java.time.LocalTime;
import java.util.Collections; // <-- Import Collections
import java.util.List;
import java.util.Optional;
import java.util.Set;

// --- Static imports for assertions and mocking ---
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    // Tell Mockito to create fake versions of these
    @Mock
    private CourseRepository courseRepo;

    @Mock
    private StudentCourseRepository studentCourseRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private Authentication auth;

    @Mock
    private RedirectAttributes redirectAttrs;

    @Mock // <-- NEW MOCK (needed for GET methods)
    private Model model;

    // Tell Mockito to inject the mocks above into our controller
    @InjectMocks
    private StudentController studentController;

    // ============== EXISTING TESTS (UNCHANGED) ==============

    @Test
    void testEnroll_Success_When_AllConditionsMet() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course newCourse = new Course();
        newCourse.setId(2L);
        newCourse.setName("CS 102");
        newCourse.setCredits(3);
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.WEDNESDAY));
        newCourse.setStartTime(LocalTime.of(10, 30));
        newCourse.setEndTime(LocalTime.of(11, 30));

        // --- 2. DEFINE MOCK BEHAVIOR ---
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(courseRepo.findById(2L)).thenReturn(Optional.of(newCourse));
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of());

        // --- 3. ACT ---
        String viewName = studentController.enrollCourse(2L, auth, redirectAttrs);

        // --- 4. ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        verify(studentCourseRepo, times(1)).save(any(StudentCourse.class));
        verify(redirectAttrs).addFlashAttribute("success", "Successfully enrolled in CS 102!");
    }

    @Test
    void testEnroll_Fails_When_CreditLimitExceeded() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course existingCourse = new Course();
        existingCourse.setId(1L);
        existingCourse.setCredits(18); // <-- Student is at 18 credits
        StudentCourse existingEnrollment = new StudentCourse(1L, student, existingCourse);

        Course newCourse = new Course();
        newCourse.setId(2L);
        newCourse.setCredits(3); // <-- This will push the total to 21

        // --- 2. DEFINE MOCK BEHAVIOR ---
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(courseRepo.findById(2L)).thenReturn(Optional.of(newCourse));
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 3. ACT ---
        String viewName = studentController.enrollCourse(2L, auth, redirectAttrs);

        // --- 4. ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));
        verify(redirectAttrs).addFlashAttribute("error", "You cannot exceed 20 credits in total.");
    }

    @Test
    void testEnroll_Fails_When_AlreadyEnrolled() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course existingCourse = new Course();
        existingCourse.setId(1L); // <-- Note the ID is 1
        existingCourse.setName("CS 101");
        StudentCourse existingEnrollment = new StudentCourse(1L, student, existingCourse);

        Course newCourse = existingCourse;

        // --- 2. DEFINE MOCK BEHAVIOR ---
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(courseRepo.findById(1L)).thenReturn(Optional.of(newCourse));
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 3. ACT ---
        String viewName = studentController.enrollCourse(1L, auth, redirectAttrs);

        // --- 4. ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));
        verify(redirectAttrs).addFlashAttribute("error", "You are already enrolled in this course.");
    }

    @Test
    void testEnroll_Fails_When_ScheduleConflictExists() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course existingCourse = new Course();
        existingCourse.setId(1L);
        existingCourse.setName("CS 101");
        existingCourse.setDaysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        existingCourse.setStartTime(LocalTime.of(10, 0)); // 10:00 AM
        existingCourse.setEndTime(LocalTime.of(11, 0));   // 11:00 AM
        StudentCourse existingEnrollment = new StudentCourse(1L, student, existingCourse);

        Course newCourse = new Course();
        newCourse.setId(2L);
        newCourse.setName("CS 102");
        newCourse.setCredits(3);
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.WEDNESDAY));
        newCourse.setStartTime(LocalTime.of(10, 30)); // 10:30 AM
        newCourse.setEndTime(LocalTime.of(11, 30));   // 11:30 AM (This conflicts!)

        // --- 2. DEFINE MOCK BEHAVIOR ---
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(courseRepo.findById(2L)).thenReturn(Optional.of(newCourse));
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 3. ACT ---
        String viewName = studentController.enrollCourse(2L, auth, redirectAttrs);

        // --- 4. ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));
        verify(redirectAttrs).addFlashAttribute("error", "Schedule conflict with CS 101");
    }

    // ============== NEW TESTS TO INCREASE COVERAGE ==============

    /**
     * NEW TEST
     * Covers: studentDashboard()
     */
    @Test
    void testStudentDashboard() {
        // --- ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        List<StudentCourse> registrations = List.of(new StudentCourse());
        List<Course> allCourses = List.of(new Course(), new Course());

        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(studentCourseRepo.findByStudent(student)).thenReturn(registrations);
        when(courseRepo.findAll()).thenReturn(allCourses);

        // --- ACT ---
        String viewName = studentController.studentDashboard(model, auth);

        // --- ASSERT ---
        assertEquals("student-dashboard", viewName);
        verify(model).addAttribute("registrations", registrations);
        verify(model).addAttribute("allCourses", allCourses);
    }

    /**
     * NEW TEST
     * Covers: unenrollCourse() success path
     */
    @Test
    void testUnenroll_Success() {
        // --- ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course courseToDrop = new Course();
        courseToDrop.setId(100L);
        courseToDrop.setName("Course to Drop");
        StudentCourse enrollment = new StudentCourse(1L, student, courseToDrop);

        // Mock finding the student
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        // Mock finding the student's enrollments
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(enrollment));

        // Capture the object that is deleted
        ArgumentCaptor<StudentCourse> captor = ArgumentCaptor.forClass(StudentCourse.class);

        // --- ACT ---
        String viewName = studentController.unenrollCourse(100L, auth, redirectAttrs);

        // --- ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        // Verify delete was called
        verify(studentCourseRepo).delete(captor.capture());
        // Verify the correct enrollment was deleted
        assertEquals(100L, captor.getValue().getCourse().getId());
        // Verify success message
        verify(redirectAttrs).addFlashAttribute("success", "Successfully unenrolled from Course to Drop.");
    }

    /**
     * NEW TEST
     * Covers: unenrollCourse() failure path (not enrolled)
     */
    @Test
    void testUnenroll_Fails_When_NotEnrolled() {
        // --- ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);

        // Mock finding the student
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        // Mock finding ZERO enrollments
        when(studentCourseRepo.findByStudent(student)).thenReturn(Collections.emptyList());

        // --- ACT ---
        // Student tries to drop course 999, which they are not in
        String viewName = studentController.unenrollCourse(999L, auth, redirectAttrs);

        // --- ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        // Verify delete was NEVER called
        verify(studentCourseRepo, never()).delete(any());
        // Verify error message
        verify(redirectAttrs).addFlashAttribute("error", "You are not enrolled in this course.");
    }

    /**
     * NEW TEST
     * Covers: viewSyllabus() success path
     */
    @Test
    void testViewSyllabus_Success_When_Enrolled() {
        // --- ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course course = new Course();
        course.setId(100L);
        course.setSyllabusText("Test Syllabus Text");
        course.setSyllabusPdfPath("/syllabi/test.pdf");
        StudentCourse enrollment = new StudentCourse(1L, student, course);

        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(courseRepo.findById(100L)).thenReturn(Optional.of(course));
        // Mock that the student IS enrolled
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(enrollment));

        // --- ACT ---
        String viewName = studentController.viewSyllabus(100L, auth, model, redirectAttrs);

        // --- ASSERT ---
        assertEquals("student-course-syllabus", viewName);
        verify(model).addAttribute("course", course);
        verify(model).addAttribute("syllabusText", "Test Syllabus Text");
        verify(model).addAttribute("syllabusPdfPath", "/syllabi/test.pdf");
        verify(model).addAttribute("hasPdf", true);
    }

    /**
     * NEW TEST
     * Covers: viewSyllabus() failure path (course not found)
     */
    @Test
    void testViewSyllabus_Fails_When_CourseNotFound() {
        // --- ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        // Mock that course is NOT found
        when(courseRepo.findById(999L)).thenReturn(Optional.empty());

        // --- ACT ---
        String viewName = studentController.viewSyllabus(999L, auth, model, redirectAttrs);

        // --- ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        verify(redirectAttrs).addFlashAttribute("error", "Course not found.");
        verify(model, never()).addAttribute(any(), any());
    }

    /**
     * NEW TEST
     * Covers: viewSyllabus() failure path (not enrolled)
     */
    @Test
    void testViewSyllabus_Fails_When_NotEnrolled() {
        // --- ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course course = new Course();
        course.setId(100L);

        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(courseRepo.findById(100L)).thenReturn(Optional.of(course));
        // Mock that the student is NOT enrolled (empty list)
        when(studentCourseRepo.findByStudent(student)).thenReturn(Collections.emptyList());

        // --- ACT ---
        String viewName = studentController.viewSyllabus(100L, auth, model, redirectAttrs);

        // --- ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        verify(redirectAttrs).addFlashAttribute("error", "You must be enrolled to view this syllabus.");
        verify(model, never()).addAttribute(any(), any());
    }
}