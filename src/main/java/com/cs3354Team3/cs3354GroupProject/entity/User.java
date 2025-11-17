package com.cs3354Team3.cs3354GroupProject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

// Each user has its own ID, email, and password and is either a student, teacher, or sys admin
@Entity
@Data
@NoArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    public User(Long id, String email, String password, Role role)
    {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}