package com.cs3354Team3.cs3354GroupProject.controller;

import com.cs3354Team3.cs3354GroupProject.entity.Course;
import com.cs3354Team3.cs3354GroupProject.entity.StudentCourse;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.cs3354Team3.cs3354GroupProject.entity.Role;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private StudentCourseRepository studentCourseRepo;

    // Listens for GET requests from front-end
    // Sends information about students, teachers, and courses to admin-dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<User> students = userRepo.findByRole(Role.STUDENT);
        List<User> teachers = userRepo.findByRole(Role.TEACHER);
        List<Course> courses = courseRepo.findAll();

        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        model.addAttribute("courses", courses);
        model.addAttribute("newCourse", new Course());
        model.addAttribute("days", DayOfWeek.values());

        return "admin-dashboard";
    }

    // Create a new course with assigned teacher and meeting time
    // Sends course to university database
    @PostMapping("/create-course")
    public String createCourse(@ModelAttribute Course newCourse, @RequestParam Long teacherId, RedirectAttributes redirectAttrs) {
        Optional<User> teacherOpt = userRepo.findById(teacherId);

        // Check that at least one teacher exists first
        if (teacherOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Invalid teacher ID.");
            return "redirect:/admin/dashboard";
        }

        // Validate date and time of course
        if (newCourse.getDaysOfWeek() == null || newCourse.getDaysOfWeek().isEmpty()
                || newCourse.getStartTime() == null
                || newCourse.getEndTime() == null) {
            redirectAttrs.addFlashAttribute("error", "You must specify meeting days and times for the course.");
            return "redirect:/admin/dashboard";
        }

        // Set teacher for course
        newCourse.setTeacher(teacherOpt.get());
        courseRepo.save(newCourse);

        redirectAttrs.addFlashAttribute("success", "Course created successfully.");
        return "redirect:/admin/dashboard";
    }

    // Unenroll all students from the course and then delete course
    @PostMapping("/delete-course")
    public String deleteCourse(@RequestParam Long courseId, RedirectAttributes redirectAttrs) {
        Optional<Course> courseOpt = courseRepo.findById(courseId);
        if (courseOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Course not found.");
            return "redirect:/admin/dashboard";
        }
        Course course = courseOpt.get();

        // Unenroll all students in the course
        List<StudentCourse> enrollments = studentCourseRepo.findByCourse(course);
        studentCourseRepo.deleteAll(enrollments);

        // Delete course
        courseRepo.delete(course);
        redirectAttrs.addFlashAttribute("success", "Course deleted successfully.");
        return "redirect:/admin/dashboard";
    }

    // First checks that student and course are valid, then enrolls student in course if not already enrolled
    @PostMapping("/enroll-student")
    public String enrollStudent(@RequestParam Long studentId, @RequestParam Long courseId, RedirectAttributes redirectAttrs) {
        Optional<User> studentOpt = userRepo.findById(studentId);
        Optional<Course> courseOpt = courseRepo.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Invalid student or course ID.");
            return "redirect:/admin/dashboard";
        }

        User student = studentOpt.get();
        Course course = courseOpt.get();

        // Check if already enrolled
        boolean alreadyEnrolled = studentCourseRepo.findByStudent(student).stream().anyMatch(sc -> sc.getCourse().getId().equals(courseId));
        if (alreadyEnrolled) {
            redirectAttrs.addFlashAttribute("error", "Student is already enrolled in this course.");
            return "redirect:/admin/dashboard";
        }

        // If not yet enrolled, enroll student in course
        StudentCourse sc = new StudentCourse();
        sc.setStudent(student);
        sc.setCourse(course);
        studentCourseRepo.save(sc);

        redirectAttrs.addFlashAttribute("success", "Student enrolled in course successfully.");
        return "redirect:/admin/dashboard";
    }

    // Validate student and course and then drop course from students schedule
    @PostMapping("/unenroll-student")
    public String unenrollStudent(@RequestParam Long studentId, @RequestParam Long courseId, RedirectAttributes redirectAttrs) {
        User student = userRepo.findById(studentId).orElse(null);
        Course course = courseRepo.findById(courseId).orElse(null);

        if (student == null || course == null) {
            redirectAttrs.addFlashAttribute("error", "Invalid student or course.");
            return "redirect:/admin/dashboard";
        }

        List<StudentCourse> enrollments = studentCourseRepo.findByStudent(student);
        enrollments.stream().filter(sc -> sc.getCourse().getId().equals(courseId)).forEach(studentCourseRepo::delete);

        redirectAttrs.addFlashAttribute("success", "Student unenrolled from course successfully.");
        return "redirect:/admin/dashboard";
    }
}
