package com.cs3354Team3.cs3354GroupProject.service;

import com.cs3354Team3.cs3354GroupProject.entity.Course;
import com.cs3354Team3.cs3354GroupProject.entity.StudentCourse;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private StudentCourseRepository studentCourseRepo;

    @Autowired
    private AdminService adminService;

    /**
     * FR#4: Helper method to get a student's enrolled courses.
     */
    public List<StudentCourse> getEnrolledCourses(Long studentId) {
        User student = userRepo.findById(studentId).orElse(null);
        if (student == null) {
            return Collections.emptyList();
        }
        return studentCourseRepo.findByStudent(student);
    }

    public String enrollCourse(Long studentId, Long courseId, RedirectAttributes redirectAttrs) {
        User student = userRepo.findById(studentId).orElse(null);
        Course course = courseRepo.findById(courseId).orElse(null);

        if (student == null || course == null) {
            redirectAttrs.addFlashAttribute("error", "Invalid student or course ID.");
            return "redirect:/student-dashboard";
        }

        List<StudentCourse> studentCourses = studentCourseRepo.findByStudent(student);

        // FR#11: Check Registration Time (NOT IMPLEMENTED)
        // if (LocalDate.now().isBefore(student.getRegistrationDate())) {
        //    redirectAttrs.addFlashAttribute("error", "Your registration window has not opened yet.");
        //    return "redirect:/student-dashboard";
        // }

        // FR#6: Check for prerequisites
        if (course.getPrereqCourseId() != null) {
            Set<Long> completedCourseIds = studentCourses.stream()
                    .map(studentCourse -> studentCourse.getCourse().getId())
                    .collect(Collectors.toSet());

            if (!completedCourseIds.contains(course.getPrereqCourseId())) {
                redirectAttrs.addFlashAttribute("error", "Prerequisite not met.");
                return "redirect:/student-dashboard";
            }
        }

        // FR#8: Check for time conflicts
        if (hasTimeConflict(studentCourses, course)) {
            redirectAttrs.addFlashAttribute("error", "Time conflict with existing course.");
            return "redirect:/student-dashboard";
        }

        // FR#7: Check credit hour limit
        int currentCredits = calculateTotalCredits(studentCourses);
        if (currentCredits + course.getCredits() > 18) {
            redirectAttrs.addFlashAttribute("error", "Enrolling in this course would exceed the 18 credit hour limit.");
            return "redirect:/student-dashboard";
        }

        // FR#9: Check if course is full
        List<StudentCourse> enrolledStudents = studentCourseRepo.findByCourse(course);
        // BUG FIX: Changed from course.getCredits() to course.getCapacity()
        if (enrolledStudents.size() >= course.getCapacity()) {

            // FR#10: Waitlist Logic (NOT IMPLEMENTED)
            // if (course.isWaitlistAvailable()) {
            //    waitlistRepo.save(new Waitlist(student, course));
            //    redirectAttrs.addFlashAttribute("success", "Added to waitlist for " + course.getName());
            //    return "redirect:/student-dashboard";
            // }

            redirectAttrs.addFlashAttribute("error", "Course is full.");
            return "redirect:/student-dashboard";
        }

        // All checks passed. Enroll the student.
        StudentCourse newEnrollment = new StudentCourse(null, student, course);
        studentCourseRepo.save(newEnrollment);

        // Update student's credit count
        student.setCredits(currentCredits + course.getCredits());
        userRepo.save(student);

        redirectAttrs.addFlashAttribute("success", "Enrolled in course successfully.");
        return "redirect:/student-dashboard";
    }

    public String unenrollCourse(Long studentId, Long courseId, RedirectAttributes redirectAttrs) {
        User student = userRepo.findById(studentId).orElse(null);
        Course course = courseRepo.findById(courseId).orElse(null);

        if (student == null || course == null) {
            redirectAttrs.addFlashAttribute("error", "Invalid student or course ID.");
            return "redirect:/student-dashboard";
        }

        // FR#12: Check drop deadline
        LocalDate dropDeadline = adminService.getDropDeadline();
        if (LocalDate.now().isAfter(dropDeadline)) {
            redirectAttrs.addFlashAttribute("error", "The drop deadline has passed.");
            return "redirect:/student-dashboard";
        }

        List<StudentCourse> studentCourses = studentCourseRepo.findByStudent(student);
        StudentCourse enrollmentToRemove = null;
        for (StudentCourse enrollment : studentCourses) {
            if (enrollment.getCourse().getId().equals(courseId)) {
                enrollmentToRemove = enrollment;
                break;
            }
        }

        if (enrollmentToRemove != null) {
            studentCourseRepo.delete(enrollmentToRemove);

            // Update student's credit count
            int currentCredits = calculateTotalCredits(studentCourses);
            student.setCredits(currentCredits - course.getCredits());
            userRepo.save(student);

            redirectAttrs.addFlashAttribute("success", "Unenrolled from course successfully.");
        } else {
            redirectAttrs.addFlashAttribute("error", "You are not enrolled in this course.");
        }

        return "redirect:/student-dashboard";
    }

    private int calculateTotalCredits(List<StudentCourse> studentCourses) {
        return studentCourses.stream().mapToInt(sc -> sc.getCourse().getCredits()).sum();
    }

    private boolean hasTimeConflict(List<StudentCourse> studentCourses, Course newCourse) {
        for (StudentCourse existingEnrollment : studentCourses) {
            Course existingCourse = existingEnrollment.getCourse();
            if (newCourse.getDaysOfWeek().stream().anyMatch(existingCourse.getDaysOfWeek()::contains)) {
                if (newCourse.getStartTime().isBefore(existingCourse.getEndTime()) && newCourse.getEndTime().isAfter(existingCourse.getStartTime())) {
                    return true;
                }
            }
        }
        return false;
    }
}