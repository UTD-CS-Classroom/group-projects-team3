package com.cs3354Team3.cs3354GroupProject.controller;// --- Your project's classes (THESE ARE LIKELY MISSING) ---
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// --- Spring Framework ---
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// --- Standard Java ---
import java.time.LocalTime;
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

    // Tell Mockito to inject the mocks above into our controller
    @InjectMocks
    private StudentController studentController;

    @Test
    void testEnroll_Success_When_AllConditionsMet() {
        // --- 1. ARRANGE ---

        // Create a fake student
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);

        // Create the NEW course the student is trying to add
        Course newCourse = new Course();
        newCourse.setId(2L);
        newCourse.setName("CS 102");
        newCourse.setCredits(3);
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.WEDNESDAY));
        newCourse.setStartTime(LocalTime.of(10, 30));
        newCourse.setEndTime(LocalTime.of(11, 30));

        // --- 2. DEFINE MOCK BEHAVIOR ---

        // When auth.getName() is called, return "student@test.com"
        when(auth.getName()).thenReturn("student@test.com");

        // When userRepo.findByEmail(...) is called, return our fake student
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));

        // When courseRepo.findById(...) is called, return our fake new course
        when(courseRepo.findById(2L)).thenReturn(Optional.of(newCourse));

        // *** KEY: Return an EMPTY LIST for existing courses ***
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of());

        // --- 3. ACT ---

        // Call the method we are testing
        String viewName = studentController.enrollCourse(2L, auth, redirectAttrs);

        // --- 4. ASSERT ---

        // Check that it redirects back to the dashboard
        assertEquals("redirect:/student/dashboard", viewName);

        // *** KEY: Check that the "save" method WAS CALLED ***
        verify(studentCourseRepo, times(1)).save(any(StudentCourse.class));

        // Check that the correct success message was added
        verify(redirectAttrs).addFlashAttribute("success", "Successfully enrolled in CS 102!");
    }

    @Test
    void testEnroll_Fails_When_CreditLimitExceeded() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);

        // Create an existing course worth 18 credits
        Course existingCourse = new Course();
        existingCourse.setId(1L);
        existingCourse.setCredits(18); // <-- Student is at 18 credits

        StudentCourse existingEnrollment = new StudentCourse(1L, student, existingCourse);

        // Create the NEW course worth 3 credits
        Course newCourse = new Course();
        newCourse.setId(2L);
        newCourse.setCredits(3); // <-- This will push the total to 21

        // --- 2. DEFINE MOCK BEHAVIOR ---
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(courseRepo.findById(2L)).thenReturn(Optional.of(newCourse));

        // Return the list with the 18-credit course
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 3. ACT ---
        String viewName = studentController.enrollCourse(2L, auth, redirectAttrs);

        // --- 4. ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);

        // Check that "save" was NEVER called
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));

        // Check that the correct error message was added
        verify(redirectAttrs).addFlashAttribute("error", "You cannot exceed 20 credits in total.");
    }

    @Test
    void testEnroll_Fails_When_AlreadyEnrolled() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);

        // Create the course the student is ALREADY enrolled in
        Course existingCourse = new Course();
        existingCourse.setId(1L); // <-- Note the ID is 1
        existingCourse.setName("CS 101");

        StudentCourse existingEnrollment = new StudentCourse(1L, student, existingCourse);

        // Create a "new" course that is actually the SAME course
        Course newCourse = existingCourse; // We can just re-use the same object

        // --- 2. DEFINE MOCK BEHAVIOR ---
        when(auth.getName()).thenReturn("student@test.com");
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));

        // *** KEY: We are trying to enroll in course with ID 1 ***
        when(courseRepo.findById(1L)).thenReturn(Optional.of(newCourse));

        // *** KEY: The repo returns the course we're trying to add ***
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 3. ACT ---

        // *** KEY: Pass the ID of the course the student is already in ***
        String viewName = studentController.enrollCourse(1L, auth, redirectAttrs);

        // --- 4. ASSERT ---
        assertEquals("redirect:/student/dashboard", viewName);
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));
        verify(redirectAttrs).addFlashAttribute("error", "You are already enrolled in this course.");
    }

    @Test
    void testEnroll_Fails_When_ScheduleConflictExists() {
        // --- 1. ARRANGE ---

        // Create a fake student
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);

        // Create the course the student is ALREADY enrolled in
        Course existingCourse = new Course();
        existingCourse.setId(1L); // <-- ADD THIS LINE
        existingCourse.setName("CS 101");
        existingCourse.setDaysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        existingCourse.setStartTime(LocalTime.of(10, 0)); // 10:00 AM
        existingCourse.setEndTime(LocalTime.of(11, 0));   // 11:00 AM

        // Wrap it in the StudentCourse entity
        StudentCourse existingEnrollment = new StudentCourse(1L, student, existingCourse);

        // Create the NEW course the student is trying to add
        Course newCourse = new Course();
        newCourse.setId(2L);
        newCourse.setName("CS 102");
        newCourse.setCredits(3);
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.WEDNESDAY));
        newCourse.setStartTime(LocalTime.of(10, 30)); // 10:30 AM
        newCourse.setEndTime(LocalTime.of(11, 30));   // 11:30 AM (This conflicts!)

        // --- 2. DEFINE MOCK BEHAVIOR ---

        // When auth.getName() is called, return "student@test.com"
        when(auth.getName()).thenReturn("student@test.com");

        // When userRepo.findByEmail(...) is called, return our fake student
        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(student));

        // When courseRepo.findById(...) is called, return our fake new course
        when(courseRepo.findById(2L)).thenReturn(Optional.of(newCourse));

        // When studentCourseRepo.findByStudent(...) is called, return the list with the existing course
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));


        // --- 3. ACT ---

        // Call the method we are testing
        String viewName = studentController.enrollCourse(2L, auth, redirectAttrs);


        // --- 4. ASSERT ---

        // Check that it redirects back to the dashboard
        assertEquals("redirect:/student/dashboard", viewName);

        // Check that the "save" method was NEVER called
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));

        // Check that the correct error message was added
        verify(redirectAttrs).addFlashAttribute("error", "Schedule conflict with CS 101");
    }
}