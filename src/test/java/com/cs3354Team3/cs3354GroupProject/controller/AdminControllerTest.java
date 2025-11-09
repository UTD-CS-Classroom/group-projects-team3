package com.cs3354Team3.cs3354GroupProject.controller;

import com.cs3354Team3.cs3354GroupProject.entity.*;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    // --- Mocks ---
    @Mock
    private UserRepository userRepo;

    @Mock
    private CourseRepository courseRepo;

    @Mock
    private StudentCourseRepository studentCourseRepo;

    @Mock
    private RedirectAttributes redirectAttrs;

    // --- Class Under Test ---
    @InjectMocks
    private AdminController adminController;

    /**
     * Requirement: Admin can create a new course and assign a teacher.
     * Links to: FR #13
     */
    @Test
    void testCreateCourse_Success() {
        // --- 1. ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course newCourse = new Course();
        newCourse.setName("New CS Course");
        // Add valid days and times to pass the controller's validation check
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.MONDAY));
        newCourse.setStartTime(LocalTime.of(9, 0));
        newCourse.setEndTime(LocalTime.of(10, 0));

        when(userRepo.findById(1L)).thenReturn(Optional.of(teacher));

        // --- 2. ACT ---
        String viewName = adminController.createCourse(newCourse, 1L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        // Verify the course was saved
        verify(courseRepo, times(1)).save(newCourse);
        // Verify the teacher was set on the course object
        assertEquals(teacher, newCourse.getTeacher());
        verify(redirectAttrs).addFlashAttribute("success", "Course created successfully.");
    }

    /**
     * Requirement: Admin cannot create a course with invalid data.
     * Links to: FR #13 (Validation)
     * NEW TEST
     */
    @Test
    void testCreateCourse_Fails_When_TimesMissing() {
        // --- 1. ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course newCourse = new Course(); // Note: No times or days are set
        newCourse.setName("New CS Course");

        when(userRepo.findById(1L)).thenReturn(Optional.of(teacher));

        // --- 2. ACT ---
        String viewName = adminController.createCourse(newCourse, 1L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        // Verify the course was NEVER saved
        verify(courseRepo, never()).save(any(Course.class));
        // Verify the error message was added
        verify(redirectAttrs).addFlashAttribute("error", "You must specify meeting days and times for the course.");
    }


    /**
     * Requirement: Admin can enroll a student in a course.
     * Links to: FR #14
     */
    @Test
    void testEnrollStudent_Success() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course course = new Course();
        course.setId(10L);

        // Mock repository calls
        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(10L)).thenReturn(Optional.of(course));
        // Mock that student is not already enrolled
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of());

        // --- 2. ACT ---
        String viewName = adminController.enrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        // Verify save was called
        verify(studentCourseRepo, times(1)).save(any(StudentCourse.class));
        // Verify success message was added
        verify(redirectAttrs).addFlashAttribute("success", "Student enrolled in course successfully.");
    }

    /**
     * Requirement: Admin cannot enroll a student who is already enrolled.
     * Links to: FR #14
     */
    @Test
    void testEnrollStudent_Fails_When_AlreadyEnrolled() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course course = new Course();
        course.setId(10L);
        StudentCourse existingEnrollment = new StudentCourse(1L, student, course);

        // Mock repository calls
        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(10L)).thenReturn(Optional.of(course));
        // Mock that student IS already enrolled
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 2. ACT ---
        String viewName = adminController.enrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        // Verify save was NEVER called
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));
        // Verify error message was added
        verify(redirectAttrs).addFlashAttribute("error", "Student is already enrolled in this course.");
    }

    /**
     * Requirement: Admin cannot enroll a student if IDs are invalid.
     * Links to: FR #14 (Validation)
     * NEW TEST
     */
    @Test
    void testEnrollStudent_Fails_InvalidIDs() {
        // --- 1. ARRANGE ---
        // Mock that the student ID (1L) does not exist
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        // Mock that the course ID (10L) *does* exist (for this test case)
        when(courseRepo.findById(10L)).thenReturn(Optional.of(new Course()));

        // --- 2. ACT ---
        String viewName = adminController.enrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));
        verify(redirectAttrs).addFlashAttribute("error", "Invalid student or course ID.");
    }

    /**
     * Requirement: Admin can drop a student from a course.
     * Links to: FR #14
     * NEW TEST
     */
    @Test
    void testUnenrollStudent_Success() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course course = new Course();
        course.setId(10L);
        StudentCourse existingEnrollment = new StudentCourse(1L, student, course);

        // Mock finding the student and course
        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(10L)).thenReturn(Optional.of(course));
        // Mock finding the enrollment
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 2. ACT ---
        String viewName = adminController.unenrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        // Verify the 'delete' method was called on the repo
        verify(studentCourseRepo, times(1)).delete(existingEnrollment);
        verify(redirectAttrs).addFlashAttribute("success", "Student unenrolled from course successfully.");
    }

    /**
     * Requirement: Admin can delete any course.
     * Links to: FR #14 (implied)
     */
    @Test
    void testDeleteCourse_Success() {
        // --- 1. ARRANGE ---
        Course courseToDelete = new Course();
        courseToDelete.setId(10L);
        StudentCourse mockEnrollment = new StudentCourse(); // Mock an existing enrollment

        when(courseRepo.findById(10L)).thenReturn(Optional.of(courseToDelete));
        // Mock that enrollments were found for this course
        when(studentCourseRepo.findByCourse(courseToDelete)).thenReturn(List.of(mockEnrollment));

        // --- 2. ACT ---
        String viewName = adminController.deleteCourse(10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        // Verify the enrollments were deleted first
        verify(studentCourseRepo, times(1)).deleteAll(List.of(mockEnrollment));
        // Verify the course itself was deleted
        verify(courseRepo, times(1)).delete(courseToDelete);
        verify(redirectAttrs).addFlashAttribute("success", "Course deleted successfully.");
    }
}