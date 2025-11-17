package com.cs3354Team3.cs3354GroupProject;

import com.cs3354Team3.cs3354GroupProject.entity.*;
import com.cs3354Team3.cs3354GroupProject.repository.CourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.StudentCourseRepository;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class Cs3354GroupProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(Cs3354GroupProjectApplication.class, args);
    }

    /**
     * This CommandLineRunner bean will run once on application startup.
     * It checks if the database is empty (userRepo.count() == 0) and, if so,
     * populates it with a complete set of test data.
     */
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo,
                                   CourseRepository courseRepo,
                                   StudentCourseRepository studentCourseRepo,
                                   BCryptPasswordEncoder encoder) {
        return args -> {
            // Only populate if the database is empty
            if (userRepo.count() == 0) {
                System.out.println("Database is empty. Seeding data...");

                // --- 1. Create Users ---
                // Password for all users is "pass123"
                String password = encoder.encode("pass123");

                User admin = new User(null, "admin@test.com", password, Role.ADMIN);
                User teacher1 = new User(null, "teacher@test.com", password, Role.TEACHER);
                User teacher2 = new User(null, "prof.davis@test.com", password, Role.TEACHER);
                User student1 = new User(null, "student@test.com", password, Role.STUDENT);
                User student2 = new User(null, "jane.doe@test.com", password, Role.STUDENT);

                // Save users
                userRepo.saveAll(List.of(admin, teacher1, teacher2, student1, student2));
                System.out.println("Created 5 users.");

                // --- 2. Create Courses ---
                // NOTE: Course constructor now has two extra fields:
                // (Long id, String name, String description, int credits,
                //  User teacher, Set<DayOfWeek> daysOfWeek,
                //  LocalTime startTime, LocalTime endTime,
                //  String syllabusText, String syllabusPdfPath)

                Course swe = new Course(
                        null,
                        "Software Engineering",
                        "Intro to SWE",
                        3,
                        teacher1,
                        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 15),
                        null,           // syllabusText
                        null            // syllabusPdfPath
                );

                Course ds = new Course(
                        null,
                        "Data Structures",
                        "Core CS concepts",
                        3,
                        teacher2,
                        Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 45),
                        null,
                        null
                );

                Course calc = new Course(
                        null,
                        "Calculus I",
                        "Intro to Calculus",
                        4,
                        teacher1,
                        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                        LocalTime.of(13, 0),
                        LocalTime.of(14, 15),
                        null,
                        null
                );

                Course conflict = new Course(
                        null,
                        "Conflict Course",
                        "A course for testing",
                        3,
                        teacher2,
                        Set.of(DayOfWeek.MONDAY),
                        LocalTime.of(9, 30),
                        LocalTime.of(10, 45),
                        null,
                        null
                );

                // Save courses
                courseRepo.saveAll(List.of(swe, ds, calc, conflict));
                System.out.println("Created 4 courses.");

                // --- 3. Create Enrollments ---
                StudentCourse enrollment1 = new StudentCourse(null, student1, swe);   // student@test.com is in SWE
                StudentCourse enrollment2 = new StudentCourse(null, student1, calc);  // student@test.com is in Calc I
                StudentCourse enrollment3 = new StudentCourse(null, student2, ds);    // jane.doe@test.com is in DS

                studentCourseRepo.saveAll(List.of(enrollment1, enrollment2, enrollment3));
                System.out.println("Enrolled students in 3 courses.");

                System.out.println("Database seeding complete.");
            } else {
                System.out.println("Database already populated. Skipping seeding.");
            }
        };
    }
}
