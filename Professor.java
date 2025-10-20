package seProject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Professor class extending User
class Professor extends User {
 private List<Course> taughtCourses; // Association: Professor teaches Courses

 public Professor(String username, String password, String id) {
     super(username, password, id);
     this.taughtCourses = new ArrayList<>();
 }

 // Submit course (Functional Req 2)
 public void submitCourse(Course course) {
     taughtCourses.add(course);
 }

 // Provide course info (Functional Req 3)
 public void updateCourseInfo(Course course, String description, String syllabus, int maxStudents, List<Date> meetingTimes) {
     if (taughtCourses.contains(course)) {
         course.setDescription(description);
         course.setSyllabus(syllabus);
         course.setMaxStudents(maxStudents);
         course.setMeetingTimes(meetingTimes);
     }
 }

 @Override
 public void performRoleActions() {
     // Professor-specific actions
 }
}
