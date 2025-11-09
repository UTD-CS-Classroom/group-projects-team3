package seProject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestSchoolSystem {
    public static void main(String[] args) {
        // Current date and time for testing
        Date currentTime = new Date(); // 09:48 PM CDT, October 18, 2025

        // Create a professor
        Professor professor = new Professor("prof1", "profpass", "P123");
        professor.performRoleActions();
        System.out.println("Professor created: " + professor.getUsername());

        // Create a course
        Course course = new Course("CS101", 3, professor);
        course.setDescription("Intro to Computer Science");
        course.setMaxStudents(2);
        List<Date> meetingTimes = new ArrayList<>();
        meetingTimes.add(currentTime); // Mock meeting time
        course.setMeetingTimes(meetingTimes);
        professor.submitCourse(course);
        System.out.println("Course submitted: " + course.getDescription());

        // Create a student
        Date regStartTime = new Date(currentTime.getTime() - 10000); // Registration started 10s ago
        Student student = new Student("student1", "pass123", "S123", "Computer Science", 12, regStartTime);
        student.performRoleActions();
        System.out.println("Student created: " + student.getUsername());

        // Create an admin
        Admin admin = new Admin("admin1", "adminpass", "A123");
        admin.performRoleActions();
        System.out.println("Admin created: " + admin.getUsername());

        // Test login
        boolean loginSuccess = student.login("student1", "pass123");
        System.out.println("Student login successful: " + loginSuccess);

        // Test search courses
        List<Course> allCourses = new ArrayList<>();
        allCourses.add(course);
        List<Course> searchResults = student.searchCourses(allCourses, "CS");
        System.out.println("Search results count: " + searchResults.size());
        if (!searchResults.isEmpty()) {
            System.out.println("Found course: " + searchResults.get(0).getDescription());
        }

        // Test add course
        boolean addSuccess = student.addCourse(course, currentTime);
        System.out.println("Add course successful: " + addSuccess);
        System.out.println("Total credits: " + student.getTotalCredits());

        // Test admin actions
        Date dropDeadline = new Date(currentTime.getTime() + 86400000); // 24 hours from now
        admin.setStudentRegistrationTime(student, regStartTime, dropDeadline);
        admin.setMaxCreditHours(student, 15);
        admin.addCourseToStudent(student, course);
        System.out.println("Admin added course, new max credits: " + student.getMaxCreditHours());

        // Test drop course
        boolean dropSuccess = student.dropCourse(course, currentTime, dropDeadline);
        System.out.println("Drop course successful: " + dropSuccess);
        System.out.println("Total credits after drop: " + student.getTotalCredits());

        // Test waitlist
        student.waitlistCourse(course);
        System.out.println("Student waitlisted for: " + course.getDescription());
    }
}