Class registration system
A full-stack university course registration platform built with Spring Boot, MySQL, HTML/CSS, and automated JUnit testing.

This README summarizes the system’s purpose, features, architecture, implementation strategy, and testing approach based on the project deliverables.

Overview

The Class Registration System allows students, professors, and administrators to manage course registration activities similar to real university enrollment. The goal is to provide a reliable and intuitive interface for course browsing, adding/dropping, professor course management, and admin overrides. The team followed an Agile workflow using short sprints and referenced Deliverables I–III for requirement traceability and architectural alignment.

Stakeholders

Students – Register for, drop, and view courses; browse catalog; receive warnings about conflicts or limits.
Professors – Create and manage the courses they teach; modify course info; upload syllabi.
Administrators – Add or drop classes for any user; manage accounts; configure registration windows and deadlines.

Functional Requirements (Implemented + Planned)

Implemented (Deliverable III):
FR#1 – User login with unique username/password
FR#2, FR#3 – Professors create and manage courses
FR#5 – Students search, add, and drop classes
FR#7, FR#8 – Max credit hours + schedule conflict enforcement
FR#13 – Admins create courses
FR#14 – Admins modify student schedules

Not Yet Implemented:
FR#4 – Track completed courses
FR#6 – Prerequisite/corequisite checking
FR#9 – Prevent registering for full classes
FR#10 – Waitlists + notifications
FR#11 – Enforce registration time windows
FR#12 – Drop deadline enforcement

Non-Functional Requirements
Secure login and restricted data access (FERPA aligned)
System availability target of 99% during registration periods
Login response within 10 seconds
Privacy and controlled access to student data

System Architecture
Patterns Used:
Layered Architecture: UI → Authentication → Business Logic → Database

MVC:
Model: Entities and repositories
View: HTML/CSS frontend pages
Controller: Student, Professor, and Admin controllers

Supporting Artifacts:
Activity diagram (student/professor/admin flows)
State machine diagram (course adding workflow)
Class diagram (core system classes)
Logical & Process Views (4+1 architecture model)

Technology Stack
Backend: Java JDK 25, Spring Boot 3.5.7, Maven 1.5.20
Frontend: HTML, CSS, JavaScript
Database: MySQL
Testing: JUnit 5, Mockito, Spring Boot Test
IDE: Primarily IntelliJ IDEA, any Java IDE supported

Automated Testing Summary
A multi-layer testing strategy was used:

Unit Tests:
StudentControllerTest
TeacherControllerTest
AdminControllerTest

Integration Tests:
RepositoryIntegrationTest
SecurityConfigTest

System Test:
StudentE2ETest

Coverage:
~70% code coverage

All tests linked to requirement IDs (FR#1, FR#2, FR#5, FR#7, FR#8, FR#13, FR#14, NFR#1, NFR#4)

Development Process

The project used an Agile approach with 1–2 week sprints. Requirements and diagrams from Deliverables I and II guided implementation decisions. The team divided work across frontend, backend, and testing tasks. Architectural patterns (MVC + layered design) ensured consistency and maintainability.

Running the Project

1. Clone the Repository:
git clone https://github.com/UTD-CS-Classroom/group-projects-team3

2. Configure MySQL:
Create database:
CREATE DATABASE registration_db;
Update credentials in application.properties.

3. Build & Run:
mvn clean install
mvn spring-boot:run

4. Access the System:
Visit:
http://localhost:8080/

Future Improvements
Waitlist system
Prerequisite engine
Admin analytics dashboard
AI-powered course recommendation tool
Two-factor authentication
Enhanced UI/UX and schedule visualization

Conclusion
This system demonstrates the complete lifecycle of a software engineering project—requirements, modeling, architecture, implementation, and automated testing. The project is structured, testable, and ready for future enhancements.
