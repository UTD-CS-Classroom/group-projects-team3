package com.cs3354Team3.cs3354GroupProject.controller;

import com.cs3354Team3.cs3354GroupProject.entity.Course;
import com.cs3354Team3.cs3354GroupProject.entity.Role;
import com.cs3354Team3.cs3354GroupProject.entity.StudentCourse;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;

// --- IMPORT CHANGES HERE ---
import com.cs3354Team3.cs3354GroupProject.entity.DayOfWeek; // Use your project's DayOfWeek
import java.util.Collections; // Import Collections for emptySet()
// --- END IMPORT CHANGES ---

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    @Mock
    private Model model;

    // --- Captors ---
    @Captor
    private ArgumentCaptor<StudentCourse> studentCourseCaptor;

    // --- Class Under Test ---
    @InjectMocks
    private AdminController adminController;


    // --- NEW TEST ---
    /**
     * Covers the adminDashboard() method.
     * This improves Method and Line coverage.
     */
    @Test
    void testAdminDashboard() {
        // --- 1. ARRANGE ---
        when(userRepo.findByRole(Role.STUDENT)).thenReturn(List.of(new User()));
        when(userRepo.findByRole(Role.TEACHER)).thenReturn(List.of(new User()));
        when(courseRepo.findAll()).thenReturn(List.of(new Course()));

        // --- 2. ACT ---
        String viewName = adminController.adminDashboard(model);

        // --- 3. ASSERT ---
        assertEquals("admin-dashboard", viewName);
        verify(model, times(1)).addAttribute(eq("students"), anyList());
        verify(model, times(1)).addAttribute(eq("teachers"), anyList());
        verify(model, times(1)).addAttribute(eq("courses"), anyList());
        verify(model, times(1)).addAttribute(eq("newCourse"), any(Course.class));
        verify(model, times(1)).addAttribute(eq("days"), any());
    }


    /**
     * Requirement: Admin can create a new course and assign a teacher.
     * Links to: FR #13
     * Covers: createCourse() "happy path"
     */
    @Test
    void testCreateCourse_Success() {
        // --- 1. ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course newCourse = new Course();
        newCourse.setName("New CS Course");

        // --- CHANGE HERE ---
        // Use your project's DayOfWeek enum (assuming it has MONDAY)
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.MONDAY));
        // --- END CHANGE ---

        newCourse.setStartTime(LocalTime.of(9, 0));
        newCourse.setEndTime(LocalTime.of(10, 0));

        when(userRepo.findById(1L)).thenReturn(Optional.of(teacher));

        // --- 2. ACT ---
        String viewName = adminController.createCourse(newCourse, 1L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(courseRepo, times(1)).save(newCourse);
        assertEquals(teacher, newCourse.getTeacher());
        verify(redirectAttrs).addFlashAttribute("success", "Course created successfully.");
    }

    // --- NEW TEST ---
    /**
     * Covers: createCourse() "sad path" for invalid teacher ID.
     * Missing Branch: if (teacherOpt.isEmpty())
     */
    @Test
    void testCreateCourse_Fails_InvalidTeacher() {
        // --- 1. ARRANGE ---
        Course newCourse = new Course();
        when(userRepo.findById(anyLong())).thenReturn(Optional.empty());

        // --- 2. ACT ---
        String viewName = adminController.createCourse(newCourse, 99L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(courseRepo, never()).save(any());
        verify(redirectAttrs).addFlashAttribute("error", "Invalid teacher ID.");
    }


    /**
     * Requirement: Admin cannot create a course with invalid data.
     * Links to: FR #13 (Validation)
     * Covers: createCourse() "sad path" for null days
     */
    @Test
    void testCreateCourse_Fails_When_TimesMissing() {
        // --- 1. ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course newCourse = new Course();
        newCourse.setName("New CS Course");
        newCourse.setDaysOfWeek(null);

        when(userRepo.findById(1L)).thenReturn(Optional.of(teacher));

        // --- 2. ACT ---
        String viewName = adminController.createCourse(newCourse, 1L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(courseRepo, never()).save(any(Course.class));
        verify(redirectAttrs).addFlashAttribute("error", "You must specify meeting days and times for the course.");
    }

    // --- NEW TEST ---
    /**
     * Covers: createCourse() "sad path" for empty days.
     * Missing Branch: newCourse.getDaysOfWeek().isEmpty()
     */
    @Test
    void testCreateCourse_Fails_When_DaysEmpty() {
        // --- 1. ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course newCourse = new Course();
        newCourse.setName("New CS Course");

        // --- CHANGE HERE ---
        // Use Collections.emptySet() to avoid the Java type error
        newCourse.setDaysOfWeek(Collections.emptySet());
        // --- END CHANGE ---

        when(userRepo.findById(1L)).thenReturn(Optional.of(teacher));

        // --- 2. ACT ---
        String viewName = adminController.createCourse(newCourse, 1L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(courseRepo, never()).save(any(Course.class));
        verify(redirectAttrs).addFlashAttribute("error", "You must specify meeting days and times for the course.");
    }

    // --- NEW TEST ---
    /**
     * Covers: createCourse() "sad path" for null start time.
     * Missing Branch: newCourse.getStartTime() == null
     */
    @Test
    void testCreateCourse_Fails_When_StartTimeNull() {
        // --- 1. ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course newCourse = new Course();
        newCourse.setName("New CS Course");
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.MONDAY)); // Days are valid
        newCourse.setStartTime(null); // Start time is null

        when(userRepo.findById(1L)).thenReturn(Optional.of(teacher));

        // --- 2. ACT ---
        String viewName = adminController.createCourse(newCourse, 1L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(courseRepo, never()).save(any(Course.class));
        verify(redirectAttrs).addFlashAttribute("error", "You must specify meeting days and times for the course.");
    }

    // --- NEW TEST ---
    /**
     * Covers: createCourse() "sad path" for null end time.
     * Missing Branch: newCourse.getEndTime() == null
     */
    @Test
    void testCreateCourse_Fails_When_EndTimeNull() {
        // --- 1. ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course newCourse = new Course();
        newCourse.setName("New CS Course");
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.MONDAY)); // Days are valid
        newCourse.setStartTime(LocalTime.of(9, 0)); // Start time is valid
        newCourse.setEndTime(null); // End time is null

        when(userRepo.findById(1L)).thenReturn(Optional.of(teacher));

        // --- 2. ACT ---
        String viewName = adminController.createCourse(newCourse, 1L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(courseRepo, never()).save(any(Course.class));
        verify(redirectAttrs).addFlashAttribute("error", "You must specify meeting days and times for the course.");
    }


    /**
     * Requirement: Admin can enroll a student in a course.
     * Links to: FR #14
     * Covers: enrollStudent() "happy path"
     */
    @Test
    void testEnrollStudent_Success() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course course = new Course();
        course.setId(10L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(10L)).thenReturn(Optional.of(course));
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of());

        // --- 2. ACT ---
        String viewName = adminController.enrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(studentCourseRepo, times(1)).save(studentCourseCaptor.capture());
        assertEquals(student, studentCourseCaptor.getValue().getStudent());
        assertEquals(course, studentCourseCaptor.getValue().getCourse());

        verify(redirectAttrs).addFlashAttribute("success", "Student enrolled in course successfully.");
    }

    /**
     * Requirement: Admin cannot enroll a student who is already enrolled.
     * Links to: FR #14
     * Covers: enrollStudent() "sad path" for duplicate enrollment
     */
    @Test
    void testEnrollStudent_Fails_When_AlreadyEnrolled() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course course = new Course();
        course.setId(10L);
        StudentCourse existingEnrollment = new StudentCourse(1L, student, course);

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(10L)).thenReturn(Optional.of(course));
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 2. ACT ---
        String viewName = adminController.enrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(studentCourseRepo, never()).save(any(StudentCourse.class));
        verify(redirectAttrs).addFlashAttribute("error", "Student is already enrolled in this course.");
    }

    /**
     * Requirement: Admin cannot enroll a student if IDs are invalid.
     * Links to: FR #14 (Validation)
     * Covers: enrollStudent() "sad path" for invalid ID
     */
    @Test
    void testEnrollStudent_Fails_InvalidIDs() {
        // --- 1. ARRANGE ---
        when(userRepo.findById(1L)).thenReturn(Optional.empty()); // Invalid student
        when(courseRepo.findById(10L)).thenReturn(Optional.of(new Course())); // Valid course

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
     * Covers: unenrollStudent() "happy path"
     */
    @Test
    void testUnenrollStudent_Success() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course course = new Course();
        course.setId(10L);
        StudentCourse existingEnrollment = new StudentCourse(1L, student, course);

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(10L)).thenReturn(Optional.of(course));
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(existingEnrollment));

        // --- 2. ACT ---
        String viewName = adminController.unenrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(studentCourseRepo, times(1)).delete(existingEnrollment);
        verify(redirectAttrs).addFlashAttribute("success", "Student unenrolled from course successfully.");
    }

    // --- NEW TEST ---
    /**
     * Covers: unenrollStudent() "sad path" for invalid student/course.
     * Missing Branch: if (student == null || course == null)
     */
    @Test
    void testUnenrollStudent_Fails_InvalidStudent() {
        // --- 1. ARRANGE ---
        when(userRepo.findById(1L)).thenReturn(Optional.empty()); // Student is null
        when(courseRepo.findById(10L)).thenReturn(Optional.of(new Course())); // Course is valid

        // --- 2. ACT ---
        String viewName = adminController.unenrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(studentCourseRepo, never()).delete(any());
        verify(redirectAttrs).addFlashAttribute("error", "Invalid student or course.");
    }

    // --- NEW TEST ---
    /**
     * Covers: unenrollStudent() "sad path" for student not in this course.
     * Missing Branch: .filter(sc -> sc.getCourse().getId().equals(courseId))
     */
    @Test
    void testUnenrollStudent_StudentNotInThisCourse() {
        // --- 1. ARRANGE ---
        User student = new User(1L, "student@test.com", "pass", Role.STUDENT);
        Course courseToDrop = new Course();
        courseToDrop.setId(10L);

        Course otherCourse = new Course(); // A different course
        otherCourse.setId(20L);
        StudentCourse otherEnrollment = new StudentCourse(1L, student, otherCourse);

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(10L)).thenReturn(Optional.of(courseToDrop));
        // Mock that the student is enrolled, but in a *different* course
        when(studentCourseRepo.findByStudent(student)).thenReturn(List.of(otherEnrollment));

        // --- 2. ACT ---
        String viewName = adminController.unenrollStudent(1L, 10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        // Verify delete was NEVER called, because the filter returned false
        verify(studentCourseRepo, never()).delete(any());
        verify(redirectAttrs).addFlashAttribute("success", "Student unenrolled from course successfully.");
    }


    /**
     * Requirement: Admin can delete any course.
     * Links to: FR #14 (implied)
     * Covers: deleteCourse() "happy path"
     */
    @Test
    void testDeleteCourse_Success() {
        // --- 1. ARRANGE ---
        Course courseToDelete = new Course();
        courseToDelete.setId(10L);
        StudentCourse mockEnrollment = new StudentCourse();

        when(courseRepo.findById(10L)).thenReturn(Optional.of(courseToDelete));
        when(studentCourseRepo.findByCourse(courseToDelete)).thenReturn(List.of(mockEnrollment));

        // --- 2. ACT ---
        String viewName = adminController.deleteCourse(10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(studentCourseRepo, times(1)).deleteAll(List.of(mockEnrollment));
        verify(courseRepo, times(1)).delete(courseToDelete);
        verify(redirectAttrs).addFlashAttribute("success", "Course deleted successfully.");
    }

    // --- NEW TEST ---
    /**
     * Covers: deleteCourse() "sad path" for invalid course ID.
     * Missing Branch: if (courseOpt.isEmpty())
     */
    @Test
    void testDeleteCourse_Fails_NotFound() {
        // --- 1. ARRANGE ---
        when(courseRepo.findById(10L)).thenReturn(Optional.empty());

        // --- 2. ACT ---
        String viewName = adminController.deleteCourse(10L, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/admin/dashboard", viewName);
        verify(studentCourseRepo, never()).deleteAll(anyList());
        verify(courseRepo, never()).delete(any());
        verify(redirectAttrs).addFlashAttribute("error", "Course not found.");
    }
}