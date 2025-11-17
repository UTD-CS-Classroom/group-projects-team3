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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /**
     * Absolute folder where PDFs will be stored.
     * On your machine this will be something like: /Users/tony/syllabi-uploads
     */
    private static final String SYLLABI_UPLOAD_DIR =
            System.getProperty("user.home") + "/syllabi-uploads";

    // ============== DASHBOARD ==============

    @GetMapping("/dashboard")
    public String teacherDashboard(Model model, Authentication auth) {
        User teacher = userRepo.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("courses", courseRepo.findByTeacher(teacher));
        return "teacher-dashboard";
    }

    // ============== ADD COURSE ==============

    @GetMapping("/add-course")
    public String addCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "add-course";
    }

    @PostMapping("/add-course")
    public String addCourseSubmit(@ModelAttribute Course course, Authentication auth) {
        User teacher = userRepo.findByEmail(auth.getName()).orElseThrow();
        course.setTeacher(teacher);
        courseRepo.save(course);
        return "redirect:/teacher/dashboard";
    }

    // ============== DELETE COURSE ==============

    @PostMapping("/delete-course")
    public String deleteCourse(@RequestParam Long courseId,
                               Authentication auth,
                               RedirectAttributes redirectAttrs) {

        User teacher = userRepo.findByEmail(auth.getName()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttrs.addFlashAttribute("error", "Course not found.");
            return "redirect:/teacher/dashboard";
        }

        if (!course.getTeacher().getId().equals(teacher.getId())) {
            redirectAttrs.addFlashAttribute("error", "You can only delete your own courses.");
            return "redirect:/teacher/dashboard";
        }

        List<StudentCourse> enrollments = studentCourseRepo.findByCourse(course);
        if (!enrollments.isEmpty()) {
            studentCourseRepo.deleteAll(enrollments);
        }

        courseRepo.delete(course);

        redirectAttrs.addFlashAttribute(
                "success",
                "Course '" + course.getName() + "' was deleted successfully."
        );
        return "redirect:/teacher/dashboard";
    }

    // ============== SYLLABUS – EDIT PAGE (TEACHER) ==============

    @GetMapping("/course/{courseId}/syllabus")
    public String editSyllabus(@PathVariable Long courseId,
                               Authentication auth,
                               Model model,
                               RedirectAttributes redirectAttrs) {

        User teacher = userRepo.findByEmail(auth.getName()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttrs.addFlashAttribute("error", "Course not found.");
            return "redirect:/teacher/dashboard";
        }

        if (!course.getTeacher().getId().equals(teacher.getId())) {
            redirectAttrs.addFlashAttribute("error", "You can only edit syllabi for your own courses.");
            return "redirect:/teacher/dashboard";
        }

        boolean hasPdf = course.getSyllabusPdfPath() != null
                && !course.getSyllabusPdfPath().isEmpty();

        model.addAttribute("course", course);
        model.addAttribute("syllabusText", course.getSyllabusText());
        model.addAttribute("syllabusPdfPath", course.getSyllabusPdfPath());
        model.addAttribute("hasPdf", hasPdf);

        // teacher-side template
        return "course-syllabus";
    }

    // ============== SYLLABUS – SAVE TEXT + PDF ==============

    @PostMapping("/course/{courseId}/syllabus")
    public String saveSyllabus(@PathVariable Long courseId,
                               @RequestParam(name = "syllabusText", required = false) String syllabusText,
                               // MUST match name="syllabusPdfFile" in the HTML form
                               @RequestParam(name = "syllabusPdfFile", required = false) MultipartFile syllabusPdf,
                               Authentication auth,
                               RedirectAttributes redirectAttrs) {

        User teacher = userRepo.findByEmail(auth.getName()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttrs.addFlashAttribute("error", "Course not found.");
            return "redirect:/teacher/dashboard";
        }

        if (!course.getTeacher().getId().equals(teacher.getId())) {
            redirectAttrs.addFlashAttribute("error", "You can only edit syllabi for your own courses.");
            return "redirect:/teacher/dashboard";
        }

        boolean textSaved = false;
        boolean pdfSaved = false;
        String pdfErrorMsg = null;

        // --- Save text directly on the course ---
        if (syllabusText != null) {
            course.setSyllabusText(syllabusText.trim());
            textSaved = true;
        }

        // --- Save PDF file, if one was uploaded ---
        if (syllabusPdf != null && !syllabusPdf.isEmpty()) {
            try {
                Path uploadRoot = Paths.get(SYLLABI_UPLOAD_DIR);
                Files.createDirectories(uploadRoot);   // make sure folder exists

                String cleanFileName = "course-" + courseId + "-" + System.currentTimeMillis() + ".pdf";
                Path target = uploadRoot.resolve(cleanFileName);

                // actually write the file
                syllabusPdf.transferTo(target.toFile());

                // web path that students will use; served by WebConfig below
                String webPath = "/syllabi/" + cleanFileName;
                course.setSyllabusPdfPath(webPath);

                pdfSaved = true;
            } catch (IOException ex) {
                ex.printStackTrace();
                pdfErrorMsg = ex.getMessage();
            }
        }

        courseRepo.save(course);

        // --- Flash messages depending on what succeeded ---
        if (textSaved && pdfSaved) {
            redirectAttrs.addFlashAttribute("success", "Syllabus text and PDF saved successfully.");
        } else if (textSaved && !pdfSaved && syllabusPdf != null && !syllabusPdf.isEmpty()) {
            String msg = "Syllabus text saved, but failed to save PDF file.";
            if (pdfErrorMsg != null && !pdfErrorMsg.isBlank()) {
                msg += " Error: " + pdfErrorMsg;
            }
            redirectAttrs.addFlashAttribute("error", msg);
        } else if (textSaved) {
            redirectAttrs.addFlashAttribute("success", "Syllabus text saved successfully.");
        } else if (pdfSaved) {
            redirectAttrs.addFlashAttribute("success", "Syllabus PDF saved successfully.");
        } else {
            redirectAttrs.addFlashAttribute("error", "Nothing to save.");
        }

        return "redirect:/teacher/course/" + courseId + "/syllabus";
    }
}
