package com.cs3354Team3.cs3354GroupProject.repository;

import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}