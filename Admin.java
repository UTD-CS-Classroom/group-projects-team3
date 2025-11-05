//test 1

package seProject;

import java.util.Date;

//Admin class extending User
class Admin extends User {

 public Admin(String username, String password, String id) {
     super(username, password, id);
 }

 // Submit course on behalf of professor (Functional Req 13)
 public void submitCourseForProfessor(Professor professor, Course course) {
     professor.submitCourse(course);
 }

 // Add/drop course for student (Functional Req 14)
 public void addCourseToStudent(Student student, Course course) {
     student.addCourse(course, new Date()); // Bypass checks
 }

 public void dropCourseFromStudent(Student student, Course course) {
     student.dropCourse(course, new Date(), new Date()); // Bypass deadline
 }

 // Set registration times and drop deadline (Functional Req 15)
 public void setStudentRegistrationTime(Student student, Date startTime, Date dropDeadline) {
     student.setRegistrationStartTime(startTime);
     // Assume global dropDeadline or per student
 }

 // Set max credit hours (Functional Req 16)
 public void setMaxCreditHours(Student student, int maxCredits) {
     student.setMaxCreditHours(maxCredits);
 }

 @Override
 public void performRoleActions() {
     // Admin-specific actions
 }
}
