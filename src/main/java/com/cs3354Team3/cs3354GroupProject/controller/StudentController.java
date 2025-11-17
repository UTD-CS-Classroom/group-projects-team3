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


import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private StudentCourseRepository studentCourseRepo;

    @Autowired
    private UserRepository userRepo;

    // Send student user details such as id and current courses to front-end dashboard
    @GetMapping("/dashboard")
    public String studentDashboard(Model model, Authentication auth) {
        User student = userRepo.findByEmail(auth.getName()).orElseThrow();
        List<StudentCourse> registrations = studentCourseRepo.findByStudent(student);
        model.addAttribute("registrations", registrations);

        List<Course> allCourses = courseRepo.findAll();
        model.addAttribute("allCourses", allCourses);
        return "student-dashboard";
    }

    // First checks that student and course are valid, then enrolls student in course if not already enrolled,
    // if does not exceed credit limit, and does not conflict with current schedule
    @PostMapping("/enroll")
    public String enrollCourse(@RequestParam Long courseId, Authentication auth, RedirectAttributes redirectAttrs) {
        User student = userRepo.findByEmail(auth.getName()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElseThrow();

        List<StudentCourse> enrolledCourses = studentCourseRepo.findByStudent(student);
        int totalCredits = enrolledCourses.stream().mapToInt(sc -> sc.getCourse().getCredits()).sum();

        // Check if already enrolled
        boolean alreadyEnrolled = studentCourseRepo.findByStudent(student).stream().anyMatch(sc -> sc.getCourse().getId().equals(courseId));
        if (alreadyEnrolled) {
            redirectAttrs.addFlashAttribute("error", "You are already enrolled in this course.");
            return "redirect:/student/dashboard";
        }

        // Check that enrolling in course does not exceed credit limit
        if (totalCredits + course.getCredits() > 20) {
            redirectAttrs.addFlashAttribute("error", "You cannot exceed 20 credits in total.");
            return "redirect:/student/dashboard";
        }

        for (StudentCourse sc : enrolledCourses) {
            Course existingCourse = sc.getCourse();

            // Check if days overlap
            boolean dayOverlap = !existingCourse.getDaysOfWeek().stream()
                    .filter(day -> course.getDaysOfWeek().contains(day))
                    .toList().isEmpty();

            // Check if time overlaps
            boolean timeOverlap = course.getStartTime().isBefore(existingCourse.getEndTime()) &&
                    course.getEndTime().isAfter(existingCourse.getStartTime());

            if (dayOverlap && timeOverlap) {
                redirectAttrs.addFlashAttribute("error", "Schedule conflict with " + existingCourse.getName());
                return "redirect:/student/dashboard";
            }
        }

        // Enroll in course if course fits in schedule
        StudentCourse sc = new StudentCourse();
        sc.setStudent(student);
        sc.setCourse(course);
        studentCourseRepo.save(sc);

        redirectAttrs.addFlashAttribute("success", "Successfully enrolled in " + course.getName() + "!");
        return "redirect:/student/dashboard";
    }

    // Check that student and course are valid and then unenroll
    @PostMapping("/unenroll")
    public String unenrollCourse(@RequestParam Long courseId, Authentication auth, RedirectAttributes redirectAttrs) {
        User student = userRepo.findByEmail(auth.getName()).orElseThrow();
        StudentCourse sc = studentCourseRepo.findByStudent(student).stream().filter(s -> s.getCourse().getId().equals(courseId)).findFirst().orElse(null);

        if (sc == null) {
            redirectAttrs.addFlashAttribute("error", "You are not enrolled in this course.");
            return "redirect:/student/dashboard";
        }

        // Unenroll in course
        studentCourseRepo.delete(sc);

        redirectAttrs.addFlashAttribute("success", "Successfully unenrolled from " + sc.getCourse().getName() + ".");
        return "redirect:/student/dashboard";
    }
}