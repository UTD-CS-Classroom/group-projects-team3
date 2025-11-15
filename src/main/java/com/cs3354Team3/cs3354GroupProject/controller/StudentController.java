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

    @GetMapping("/dashboard")
    public String studentDashboard(Model model, Authentication auth) {
        User student = userRepo.findByEmail(auth.getName()).orElseThrow();
        List<StudentCourse> registrations = studentCourseRepo.findByStudent(student);
        model.addAttribute("registrations", registrations);

        List<Course> allCourses = courseRepo.findAll();
        model.addAttribute("allCourses", allCourses);
        return "student-dashboard";
    }

    @PostMapping("/enroll")
    public String enrollCourse(@RequestParam Long courseId,
                               Authentication auth,
                               RedirectAttributes redirectAttrs) {
        User student = userRepo.findByEmail(auth.getName()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElseThrow();

        List<StudentCourse> enrolledCourses = studentCourseRepo.findByStudent(student);
        int totalCredits = enrolledCourses.stream()
                .mapToInt(sc -> sc.getCourse().getCredits())
                .sum();

        boolean alreadyEnrolled = enrolledCourses.stream()
                .anyMatch(sc -> sc.getCourse().getId().equals(courseId));

        if (alreadyEnrolled) {
            redirectAttrs.addFlashAttribute("error", "You are already enrolled in this course.");
            return "redirect:/student/dashboard";
        }

        if (totalCredits + course.getCredits() > 20) {
            redirectAttrs.addFlashAttribute("error", "You cannot exceed 20 credits in total.");
            return "redirect:/student/dashboard";
        }

        for (StudentCourse sc : enrolledCourses) {
            Course existingCourse = sc.getCourse();

            boolean dayOverlap = !existingCourse.getDaysOfWeek().stream()
                    .filter(day -> course.getDaysOfWeek().contains(day))
                    .toList().isEmpty();

            boolean timeOverlap = course.getStartTime().isBefore(existingCourse.getEndTime()) &&
                    course.getEndTime().isAfter(existingCourse.getStartTime());

            if (dayOverlap && timeOverlap) {
                redirectAttrs.addFlashAttribute("error", "Schedule conflict with " + existingCourse.getName());
                return "redirect:/student/dashboard";
            }
        }

        StudentCourse sc = new StudentCourse();
        sc.setStudent(student);
        sc.setCourse(course);
        studentCourseRepo.save(sc);

        redirectAttrs.addFlashAttribute("success", "Successfully enrolled in " + course.getName() + "!");
        return "redirect:/student/dashboard";
    }

    @PostMapping("/unenroll")
    public String unenrollCourse(@RequestParam Long courseId,
                                 Authentication auth,
                                 RedirectAttributes redirectAttrs) {
        User student = userRepo.findByEmail(auth.getName()).orElseThrow();

        StudentCourse sc = studentCourseRepo.findByStudent(student).stream()
                .filter(s -> s.getCourse().getId().equals(courseId))
                .findFirst()
                .orElse(null);

        if (sc == null) {
            redirectAttrs.addFlashAttribute("error", "You are not enrolled in this course.");
            return "redirect:/student/dashboard";
        }

        studentCourseRepo.delete(sc);

        redirectAttrs.addFlashAttribute("success",
                "Successfully unenrolled from " + sc.getCourse().getName() + ".");
        return "redirect:/student/dashboard";
    }

    // ============== STUDENT – VIEW SYLLABUS ==============

    @GetMapping("/course/{courseId}/syllabus")
    public String viewSyllabus(@PathVariable Long courseId,
                               Authentication auth,
                               Model model,
                               RedirectAttributes redirectAttrs) {

        User student = userRepo.findByEmail(auth.getName()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttrs.addFlashAttribute("error", "Course not found.");
            return "redirect:/student/dashboard";
        }

        // Optional: require enrollment to view
        boolean enrolled = studentCourseRepo.findByStudent(student).stream()
                .anyMatch(sc -> sc.getCourse().getId().equals(courseId));
        if (!enrolled) {
            redirectAttrs.addFlashAttribute("error", "You must be enrolled to view this syllabus.");
            return "redirect:/student/dashboard";
        }

        boolean hasPdf = course.getSyllabusPdfPath() != null
                && !course.getSyllabusPdfPath().isEmpty();

        model.addAttribute("course", course);
        model.addAttribute("syllabusText", course.getSyllabusText());
        model.addAttribute("syllabusPdfPath", course.getSyllabusPdfPath());
        model.addAttribute("hasPdf", hasPdf);

        return "student-course-syllabus";
    }
}
