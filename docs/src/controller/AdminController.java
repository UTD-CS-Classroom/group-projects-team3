package controller;

import entity.Course;
import entity.StudentCourse;
import entity.User;
import repository.CourseRepository;
import repository.StudentCourseRepository;
import repository.UserRepository;
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

    // --- ADMIN DASHBOARD ---
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

    @PostMapping("/create-course")
    public String createCourse(@ModelAttribute Course newCourse,
                               @RequestParam Long teacherId,
                               RedirectAttributes redirectAttrs) {
        Optional<User> teacherOpt = userRepo.findById(teacherId);

        if (teacherOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Invalid teacher ID.");
            return "redirect:/admin/dashboard";
        }

        // Validation: days and times
        if (newCourse.getDaysOfWeek() == null || newCourse.getDaysOfWeek().isEmpty()
                || newCourse.getStartTime() == null
                || newCourse.getEndTime() == null) {
            redirectAttrs.addFlashAttribute("error", "You must specify meeting days and times for the course.");
            return "redirect:/admin/dashboard";
        }

        // Assign teacher
        newCourse.setTeacher(teacherOpt.get());

        // Save course
        courseRepo.save(newCourse);

        redirectAttrs.addFlashAttribute("success", "Course created successfully.");
        return "redirect:/admin/dashboard";
    }


    // --- DELETE COURSE ---
    @PostMapping("/delete-course")
    public String deleteCourse(@RequestParam Long courseId,
                               RedirectAttributes redirectAttrs) {
        Optional<Course> courseOpt = courseRepo.findById(courseId);
        if (courseOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Course not found.");
            return "redirect:/admin/dashboard";
        }

        Course course = courseOpt.get();

        // Delete related enrollments
        List<StudentCourse> enrollments = studentCourseRepo.findByCourse(course);
        studentCourseRepo.deleteAll(enrollments);

        // Delete the course itself
        courseRepo.delete(course);

        redirectAttrs.addFlashAttribute("success", "Course deleted successfully.");
        return "redirect:/admin/dashboard";
    }

    // --- ADD COURSE TO STUDENT SCHEDULE ---
    @PostMapping("/enroll-student")
    public String enrollStudent(@RequestParam Long studentId,
                                @RequestParam Long courseId,
                                RedirectAttributes redirectAttrs) {
        Optional<User> studentOpt = userRepo.findById(studentId);
        Optional<Course> courseOpt = courseRepo.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Invalid student or course ID.");
            return "redirect:/admin/dashboard";
        }

        User student = studentOpt.get();
        Course course = courseOpt.get();

        // Prevent duplicate enrollment
        boolean alreadyEnrolled = studentCourseRepo.findByStudent(student).stream()
                .anyMatch(sc -> sc.getCourse().getId().equals(courseId));

        if (alreadyEnrolled) {
            redirectAttrs.addFlashAttribute("error", "Student is already enrolled in this course.");
            return "redirect:/admin/dashboard";
        }

        StudentCourse sc = new StudentCourse();
        sc.setStudent(student);
        sc.setCourse(course);
        studentCourseRepo.save(sc);

        redirectAttrs.addFlashAttribute("success", "Student enrolled in course successfully.");
        return "redirect:/admin/dashboard";
    }

    // --- DROP COURSE FROM STUDENT SCHEDULE ---
    @PostMapping("/unenroll-student")
    public String unenrollStudent(@RequestParam Long studentId,
                                  @RequestParam Long courseId,
                                  RedirectAttributes redirectAttrs) {
        User student = userRepo.findById(studentId).orElse(null);
        Course course = courseRepo.findById(courseId).orElse(null);

        if (student == null || course == null) {
            redirectAttrs.addFlashAttribute("error", "Invalid student or course.");
            return "redirect:/admin/dashboard";
        }

        List<StudentCourse> enrollments = studentCourseRepo.findByStudent(student);
        enrollments.stream()
                .filter(sc -> sc.getCourse().getId().equals(courseId))
                .forEach(studentCourseRepo::delete);

        redirectAttrs.addFlashAttribute("success", "Student unenrolled from course successfully.");
        return "redirect:/admin/dashboard";
    }
}
