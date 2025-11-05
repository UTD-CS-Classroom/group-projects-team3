package seProject;

import java.util.Date;

//Enrollment class for association between Student and Course
class Enrollment {
 private Student student;
 private Course course;
 private Date enrollmentDate;

 public Enrollment(Student student, Course course) {
     this.student = student;
     this.course = course;
     this.enrollmentDate = new Date();
 }

 public Student getStudent() {
     return student;
 }

 public Course getCourse() {
     return course;
 }
}
