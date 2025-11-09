package com.cs3354Team3.cs3354GroupProject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.Set;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String description;
    private int credits;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ElementCollection(targetClass = DayOfWeek.class)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;
}