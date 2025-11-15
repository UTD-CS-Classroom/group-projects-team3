package com.cs3354Team3.cs3354GroupProject.service;

import com.cs3354Team3.cs3354GroupProject.entity.Role;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// Use the Mockito extension to enable @Mock and @InjectMocks
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    // 1. Create a "fake" version of the UserRepository
    @Mock
    private UserRepository userRepository;

    // 2. Inject the fake repository into our service class
    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void testLoadUserByUsername_UserFound() {
        // --- Arrange (Given) ---
        String email = "test@example.com";
        // We create a "dummy" User object that we expect the repository to return
        User mockUser = new User(1L, email, "password123", Role.STUDENT);

        // Program the mock: "When userRepository.findByEmail(email) is called,
        // then return an Optional containing our mockUser."
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // --- Act (When) ---
        // Call the method we are testing
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // --- Assert (Then) ---
        // Verify the UserDetails object has the correct data from our mockUser
        assertNotNull(userDetails);
        assertEquals(mockUser.getEmail(), userDetails.getUsername());
        assertEquals(mockUser.getPassword(), userDetails.getPassword());

        // Check that the authority (role) was correctly set
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals(Role.STUDENT.name(), userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // --- Arrange (Given) ---
        String email = "notfound@example.com";

        // Program the mock: "When userRepository.findByEmail(email) is called,
        // then return an empty Optional."
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // --- Act & Assert (When & Then) ---
        // Verify that calling the method throws the correct exception
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });

        // (Optional) Check that the exception message is correct
        assertEquals("User not found", exception.getMessage());
    }
}