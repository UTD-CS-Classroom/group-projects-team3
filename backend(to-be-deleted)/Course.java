package seProject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Course class
class Course {
 private String name;
 private String description;
 private String syllabus;
 private int creditHours;
 private int maxStudents;
 private List<Date> meetingTimes;
 private List<Course> prerequisites; // Association: Courses have prereqs
 private List<Course> corequisites;
 private List<Student> enrolledStudents; // Aggregation: Course has students
 private List<Waitlist> waitlist; // Composition: Waitlist belongs to Course
 private Professor professor; // Association with Professor

 public Course(String name, int creditHours, Professor professor) {
     this.name = name;
     this.creditHours = creditHours;
     this.professor = professor;
     this.prerequisites = new ArrayList<>();
     this.corequisites = new ArrayList<>();
     this.enrolledStudents = new ArrayList<>();
     this.waitlist = new ArrayList<>();
 }

 public boolean isFull() {
     return enrolledStudents.size() >= maxStudents;
 }

 public void addStudent(Student student) {
     enrolledStudents.add(student);
 }

 public void removeStudent(Student student) {
     enrolledStudents.remove(student);
     if (!waitlist.isEmpty()) {
         Waitlist next = waitlist.remove(0);
         addStudent(next.getStudent());
         next.getStudent().notifySpotAvailable(this);
     }
 }

 public void addToWaitlist(Waitlist waitlistEntry) {
     waitlist.add(waitlistEntry);
 }

 // Getters/Setters
 public String getDescription() {
     return description;
 }

 public void setDescription(String description) {
     this.description = description;
 }

 public void setSyllabus(String syllabus) {
     this.syllabus = syllabus;
 }

 public void setMaxStudents(int maxStudents) {
     this.maxStudents = maxStudents;
 }

 public void setMeetingTimes(List<Date> meetingTimes) {
     this.meetingTimes = meetingTimes;
 }

 public int getCreditHours() {
     return creditHours;
 }

 public void addPrerequisite(Course prereq) {
     prerequisites.add(prereq);
 }

 public void addCorequisite(Course coreq) {
     corequisites.add(coreq);
 }
}
