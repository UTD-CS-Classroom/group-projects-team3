package seProject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors; // Added for stream().collect()

class Student extends User {
    private String major;
    private List<Course> completedCourses; // Aggregation: Student has completed courses
    private List<Enrollment> currentEnrollments; // Composition: Enrollments belong to Student
    private int maxCreditHours;
    private Date registrationStartTime;
    private List<Waitlist> waitlists; // Association with Waitlist

    public Student(String username, String password, String id, String major, int maxCreditHours, Date registrationStartTime) {
        super(username, password, id);
        this.major = major;
        this.completedCourses = new ArrayList<>();
        this.currentEnrollments = new ArrayList<>();
        this.maxCreditHours = maxCreditHours;
        this.registrationStartTime = registrationStartTime;
        this.waitlists = new ArrayList<>();
    }

    // Additional methods in Student for setters
    public void setMaxCreditHours(int maxCreditHours) {
        this.maxCreditHours = maxCreditHours;
    }

    public void setRegistrationStartTime(Date registrationStartTime) {
        this.registrationStartTime = registrationStartTime;
    }

    // Search courses (Functional Req 5)
    public List<Course> searchCourses(List<Course> allCourses, String query) {
        // Implementation: Filter courses based on query
        return allCourses.stream().filter(c -> c.getDescription().contains(query)).collect(Collectors.toList());
    }

    // Add course with checks (Functional Req 5-9, 11)
    public boolean addCourse(Course course, Date currentTime) {
        if (currentTime.before(registrationStartTime)) return false; // Req 11
        if (course.isFull()) return false; // Req 9
        if (!checkPrerequisites(course)) return false; // Req 6
        if (getTotalCredits() + course.getCreditHours() > maxCreditHours) return false; // Req 7
        if (hasTimeConflict(course)) return false; // Req 8
        currentEnrollments.add(new Enrollment(this, course));
        course.addStudent(this);
        return true;
    }

    // Drop course with deadline check (Functional Req 5, 12)
    public boolean dropCourse(Course course, Date currentTime, Date dropDeadline) {
        if (currentTime.after(dropDeadline)) return false; // Req 12
        Enrollment enrollment = findEnrollment(course);
        if (enrollment != null) {
            currentEnrollments.remove(enrollment);
            course.removeStudent(this);
            return true;
        }
        return false;
    }

    // Waitlist course (Functional Req 10)
    public void waitlistCourse(Course course) {
        Waitlist waitlist = new Waitlist(this, course);
        waitlists.add(waitlist);
        course.addToWaitlist(waitlist);
    }

    // Notify if spot available (Functional Req 10)
    public void notifySpotAvailable(Course course) {
        // Implementation: Send notification
    }

    private boolean checkPrerequisites(Course course) {
        // Check if all prereqs and coreqs are in completedCourses or currentEnrollments
        return true; // Placeholder
    }

    private boolean hasTimeConflict(Course course) {
        // Check meeting times against current enrollments
        return false; // Placeholder
    }

    int getTotalCredits() {
        return currentEnrollments.stream().mapToInt(e -> e.getCourse().getCreditHours()).sum();
    }

    private Enrollment findEnrollment(Course course) {
        return currentEnrollments.stream().filter(e -> e.getCourse().equals(course)).findFirst().orElse(null);
    }

    @Override
    public void performRoleActions() {
        // Student-specific actions
        // Placeholder: Expand as needed based on requirements
    }

    // Getters/Setters for major, completedCourses, etc.
    public String getMajor() {
        return major;
    }

    public void addCompletedCourse(Course course) {
        completedCourses.add(course);
    }

	public String getMaxCreditHours() {
		// TODO Auto-generated method stub
		return null;
	}
}