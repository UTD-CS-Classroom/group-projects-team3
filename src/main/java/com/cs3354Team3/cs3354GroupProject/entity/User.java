package com.cs3354Team3.cs3354GroupProject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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