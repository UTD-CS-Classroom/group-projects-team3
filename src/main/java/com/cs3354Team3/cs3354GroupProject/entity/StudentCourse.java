package com.cs3354Team3.cs3354GroupProject.entity;

import jakarta.persistence.*;
import lombok.*;

// All courses are pairs of students with their ID and course ID
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}