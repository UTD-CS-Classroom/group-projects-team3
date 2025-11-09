package com.cs3354Team3.cs3354GroupProject.controller;

import com.cs3354Team3.cs3354GroupProject.entity.*;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherControllerTest {

    // --- Mocks ---
    @Mock
    private CourseRepository courseRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private StudentCourseRepository studentCourseRepo;

    @Mock
    private Authentication auth;

    @Mock
    private RedirectAttributes redirectAttrs;

    // --- Class Under Test ---
    @InjectMocks
    private TeacherController teacherController;

    /**
     * Requirement: A teacher can submit a new course with all details.
     * Links to: FR #2 and FR #3
     */
    @Test
    void testAddCourse_Success_Validates_FR2_and_FR3() {
        // --- 1. ARRANGE ---
        User loggedInTeacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);

        // Create a new Course object with all the details from FR3
        Course newCourse = new Course();
        newCourse.setName("My New Course");
        newCourse.setDescription("This is a detailed description.");
        newCourse.setCredits(3);
        newCourse.setDaysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        newCourse.setStartTime(LocalTime.of(10, 0));
        newCourse.setEndTime(LocalTime.of(10, 50));
        // Note: We don't set the teacher here, the controller should do that.

        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(loggedInTeacher));

        // Create an ArgumentCaptor to "catch" the object that's saved
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);

        // --- 2. ACT ---
        String viewName = teacherController.addCourseSubmit(newCourse, auth);

        // --- 3. ASSERT ---
        assertEquals("redirect:/teacher/dashboard", viewName);

        // Verify the save method was called, and capture the object
        verify(courseRepo, times(1)).save(courseCaptor.capture());

        // Get the captured object
        Course savedCourse = courseCaptor.getValue();

        // Now, verify all the details from FR3 are correct on the saved object
        assertEquals("My New Course", savedCourse.getName());
        assertEquals("This is a detailed description.", savedCourse.getDescription());
        assertEquals(3, savedCourse.getCredits());
        assertEquals(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), savedCourse.getDaysOfWeek());
        assertEquals(LocalTime.of(10, 0), savedCourse.getStartTime());
        assertEquals(LocalTime.of(10, 50), savedCourse.getEndTime());

        // And verify the teacher was set correctly (from FR2 logic)
        assertEquals(loggedInTeacher, savedCourse.getTeacher());
    }

    /**
     * Requirement: A teacher CAN delete their OWN course.
     * Links to: FR #2, FR #3
     */
    @Test
    void testDeleteCourse_Success_When_Owner() {
        // --- 1. ARRANGE ---
        User loggedInTeacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);

        Course myCourse = new Course();
        myCourse.setId(100L);
        myCourse.setName("My Course");
        myCourse.setTeacher(loggedInTeacher);

        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(loggedInTeacher));
        when(courseRepo.findById(100L)).thenReturn(Optional.of(myCourse));
        // FIX: Mock that findByCourse returns a non-empty list
        when(studentCourseRepo.findByCourse(myCourse)).thenReturn(List.of(new StudentCourse()));

        // --- 2. ACT ---
        String viewName = teacherController.deleteCourse(100L, auth, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/teacher/dashboard", viewName);
        verify(courseRepo, times(1)).delete(myCourse);
        verify(studentCourseRepo, times(1)).deleteAll(any());
        verify(redirectAttrs).addFlashAttribute("success", "Course 'My Course' was deleted successfully.");
    }

    /**
     * Requirement: A teacher CANNOT delete another teacher's course.
     * Links to: NFR #4 (Implicit)
     */
    @Test
    void testDeleteCourse_Fails_When_NotOwner() {
        // --- 1. ARRANGE ---
        // The teacher who is logged in
        User loggedInTeacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);

        // A DIFFERENT teacher
        User otherTeacher = new User(2L, "other@test.com", "pass", Role.TEACHER);

        // A course owned by the OTHER teacher
        Course otherCourse = new Course();
        otherCourse.setId(101L);
        otherCourse.setName("Other's Course");
        otherCourse.setTeacher(otherTeacher); // Owned by someone else

        // Mock security
        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(loggedInTeacher));
        // Mock finding the course
        when(courseRepo.findById(101L)).thenReturn(Optional.of(otherCourse));

        // --- 2. ACT ---
        // LoggedInTeacher (ID 1) tries to delete a course owned by OtherTeacher (ID 2)
        String viewName = teacherController.deleteCourse(101L, auth, redirectAttrs);

        // --- 3. ASSERT ---
        assertEquals("redirect:/teacher/dashboard", viewName);
        // Verify the course was NEVER deleted
        verify(courseRepo, never()).delete(any());
        // Verify error message was added
        verify(redirectAttrs).addFlashAttribute("error", "You can only delete your own courses.");
    }
}