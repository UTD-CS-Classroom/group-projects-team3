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
import org.springframework.ui.Model; // <-- Import Model
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.Collections; // <-- Import Collections
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

    @Mock // <-- NEW MOCK (needed for GET methods)
    private Model model;

    // --- Class Under Test ---
    @InjectMocks
    private TeacherController teacherController;

    // ============== EXISTING TESTS (UNCHANGED) ==============

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


    // ============== NEW TESTS TO INCREASE COVERAGE ==============

    /**
     * NEW TEST
     * Covers: teacherDashboard()
     */
    @Test
    void testTeacherDashboard() {
        // --- ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        List<Course> courses = List.of(new Course(), new Course());

        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacher));
        when(courseRepo.findByTeacher(teacher)).thenReturn(courses);

        // --- ACT ---
        String viewName = teacherController.teacherDashboard(model, auth);

        // --- ASSERT ---
        assertEquals("teacher-dashboard", viewName);
        verify(model, times(1)).addAttribute("courses", courses);
    }

    /**
     * NEW TEST
     * Covers: addCourseForm()
     */
    @Test
    void testAddCourseForm() {
        // --- ARRANGE ---
        // (No mocks needed, but we need the 'model' mock)

        // --- ACT ---
        String viewName = teacherController.addCourseForm(model);

        // --- ASSERT ---
        assertEquals("add-course", viewName);
        // Verify a new Course object was added to the model
        verify(model, times(1)).addAttribute(eq("course"), any(Course.class));
    }

    /**
     * NEW TEST
     * Covers: deleteCourse() branch - Course not found
     */
    @Test
    void testDeleteCourse_Fails_When_CourseNotFound() {
        // --- ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacher));

        // Mock that the course is NOT found
        when(courseRepo.findById(999L)).thenReturn(Optional.empty());

        // --- ACT ---
        String viewName = teacherController.deleteCourse(999L, auth, redirectAttrs);

        // --- ASSERT ---
        assertEquals("redirect:/teacher/dashboard", viewName);
        verify(redirectAttrs).addFlashAttribute("error", "Course not found.");
        verify(courseRepo, never()).delete(any());
        verify(studentCourseRepo, never()).deleteAll(any());
    }

    /**
     * NEW TEST
     * Covers: deleteCourse() branch - No enrollments
     */
    @Test
    void testDeleteCourse_Success_With_NoEnrollments() {
        // --- ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course myCourse = new Course(100L, "My Course", "...", 3, teacher, null, null, null, null, null);

        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacher));
        when(courseRepo.findById(100L)).thenReturn(Optional.of(myCourse));

        // Mock that the enrollments list is EMPTY
        when(studentCourseRepo.findByCourse(myCourse)).thenReturn(Collections.emptyList());

        // --- ACT ---
        String viewName = teacherController.deleteCourse(100L, auth, redirectAttrs);

        // --- ASSERT ---
        assertEquals("redirect:/teacher/dashboard", viewName);
        // Verify course was deleted
        verify(courseRepo, times(1)).delete(myCourse);
        // Verify that deleteAll was NEVER called, since the list was empty
        verify(studentCourseRepo, never()).deleteAll(any());
        verify(redirectAttrs).addFlashAttribute("success", "Course 'My Course' was deleted successfully.");
    }

    /**
     * NEW TEST
     * Covers: editSyllabus() happy path
     */
    @Test
    void testEditSyllabus_HappyPath() {
        // --- ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course myCourse = new Course(100L, "My Course", "...", 3, teacher, null, null, null, null, null);
        myCourse.setSyllabusText("Existing Text");
        myCourse.setSyllabusPdfPath("/syllabi/file.pdf");

        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacher));
        when(courseRepo.findById(100L)).thenReturn(Optional.of(myCourse));

        // --- ACT ---
        String viewName = teacherController.editSyllabus(100L, auth, model, redirectAttrs);

        // --- ASSERT ---
        assertEquals("course-syllabus", viewName);
        verify(model).addAttribute("course", myCourse);
        verify(model).addAttribute("syllabusText", "Existing Text");
        verify(model).addAttribute("syllabusPdfPath", "/syllabi/file.pdf");
        verify(model).addAttribute("hasPdf", true);
    }

    /**
     * NEW TEST
     * Covers: editSyllabus() branch - Course not found
     */
    @Test
    void testEditSyllabus_Fails_When_CourseNotFound() {
        // --- ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacher));

        when(courseRepo.findById(999L)).thenReturn(Optional.empty());

        // --- ACT ---
        String viewName = teacherController.editSyllabus(999L, auth, model, redirectAttrs);

        // --- ASSERT ---
        assertEquals("redirect:/teacher/dashboard", viewName);
        verify(redirectAttrs).addFlashAttribute("error", "Course not found.");
    }

    /**
     * NEW TEST
     * Covers: saveSyllabus() branch - Text only
     */
    @Test
    void testSaveSyllabus_Success_TextOnly() {
        // --- ARRANGE ---
        User teacher = new User(1L, "teacher@test.com", "pass", Role.TEACHER);
        Course myCourse = new Course(100L, "My Course", "...", 3, teacher, null, null, null, null, null);

        when(auth.getName()).thenReturn("teacher@test.com");
        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacher));
        when(courseRepo.findById(100L)).thenReturn(Optional.of(myCourse));

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);

        // --- ACT ---
        // Pass "New Text" and a null MultipartFile
        String viewName = teacherController.saveSyllabus(100L, "  New Text  ", null, auth, redirectAttrs);

        // --- ASSERT ---
        assertEquals("redirect:/teacher/course/100/syllabus", viewName);

        // Verify the course was saved
        verify(courseRepo).save(courseCaptor.capture());
        Course savedCourse = courseCaptor.getValue();

        // Verify the text was trimmed and saved
        assertEquals("New Text", savedCourse.getSyllabusText());

        // Verify the correct success message
        verify(redirectAttrs).addFlashAttribute("success", "Syllabus text saved successfully.");
    }
}