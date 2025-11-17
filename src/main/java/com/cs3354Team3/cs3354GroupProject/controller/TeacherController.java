package com.cs3354Team3.cs3354GroupProject.controller;


import com.cs3354Team3.cs3354GroupProject.entity.Course;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.cs3354Team3.cs3354GroupProject.entity.StudentCourse;
import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private StudentCourseRepository studentCourseRepo;

    // Send teacher details such as email and courses to front-end dashboard
    @GetMapping("/dashboard")
    public String teacherDashboard(Model model, Authentication auth) {
        User teacher = userRepo.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("courses", courseRepo.findByTeacher(teacher));
        return "teacher-dashboard";
    }

    // Used for front-end to create a new course
    @GetMapping("/add-course")
    public String addCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "add-course";
    }

    // Create course and send to database, redirect to teacher dashboard
    @PostMapping("/add-course")
    public String addCourseSubmit(@ModelAttribute Course course, Authentication auth) {
        User teacher = userRepo.findByEmail(auth.getName()).orElseThrow();
        course.setTeacher(teacher);
        courseRepo.save(course);
        return "redirect:/teacher/dashboard";
    }

    // Unenroll all students and then delete course
    @PostMapping("/delete-course")
    public String deleteCourse(@RequestParam Long courseId, Authentication auth, RedirectAttributes redirectAttrs) {
        User teacher = userRepo.findByEmail(auth.getName()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttrs.addFlashAttribute("error", "Course not found.");
            return "redirect:/teacher/dashboard";
        }

        // Check that the course is being taught by correct teacher
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            redirectAttrs.addFlashAttribute("error", "You can only delete your own courses.");
            return "redirect:/teacher/dashboard";
        }

        // Unenroll students first
        List<StudentCourse> enrollments = studentCourseRepo.findByCourse(course);
        if (!enrollments.isEmpty()) {
            studentCourseRepo.deleteAll(enrollments);
        }

        // Delete the course
        courseRepo.delete(course);

        redirectAttrs.addFlashAttribute("success", "Course '" + course.getName() + "' was deleted successfully.");
        return "redirect:/teacher/dashboard";
    }
}