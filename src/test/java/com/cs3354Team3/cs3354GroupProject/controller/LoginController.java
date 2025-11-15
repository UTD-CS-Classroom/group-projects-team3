package com.cs3354Team3.cs3354GroupProject.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner; // For mocking the database init
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

// Import static methods
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class) // Tell Spring to only test this controller
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // This mock prevents the "Application failed to start" error
    // It stops the initDatabase bean from running during the test
    @MockBean
    private CommandLineRunner commandLineRunner;

    @Test
    void testLoginPage() throws Exception {
        // This test checks that an unauthenticated user gets the login page
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                // We check for text instead of the view name
                // because Spring Security serves its own default login page
                .andExpect(content().string(containsString("Please sign in")));
    }

    @Test
    void testRedirectAfterLogin_AsAdmin() throws Exception {
        // This test simulates a logged-in ADMIN user
        mockMvc.perform(get("/default")
                        .with(user("admin-user").authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().is3xxRedirection()) // Expect a redirect
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void testRedirectAfterLogin_AsTeacher() throws Exception {
        // This test simulates a logged-in TEACHER user
        mockMvc.perform(get("/default")
                        .with(user("teacher-user").authorities(new SimpleGrantedAuthority("TEACHER"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/dashboard"));
    }

    @Test
    void testRedirectAfterLogin_AsStudent() throws Exception {
        // This test simulates a logged-in STUDENT user
        mockMvc.perform(get("/default")
                        .with(user("student-user").authorities(new SimpleGrantedAuthority("STUDENT"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/dashboard"));
    }

    @Test
    void testRedirectAfterLogin_AsOtherRole() throws Exception {
        // This test checks the "default" case in your switch statement
        mockMvc.perform(get("/default")
                        .with(user("some-other-user").authorities(new SimpleGrantedAuthority("GUEST"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}