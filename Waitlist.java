package seProject;

import java.util.Date;

//Waitlist class for association between Student and Course
class Waitlist {
 private Student student;
 private Course course;
 private Date waitlistDate;

 public Waitlist(Student student, Course course) {
     this.student = student;
     this.course = course;
     this.waitlistDate = new Date();
 }

 public Student getStudent() {
     return student;
 }

 public Course getCourse() {
     return course;
 }
}
