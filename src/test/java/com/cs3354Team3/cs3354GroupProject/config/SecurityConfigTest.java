package com.cs3354Team3.cs3354GroupProject.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ... rest of the class unchanged ...


/**
 * This is an INTEGRATION TEST, not a unit test.
 * It loads the full Spring application context to test the security rules
 * defined in SecurityConfig.java.
 *
 * This test validates NFR #4: "The system shall only allow certain users to view..."
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    /**
     * Requirement: A student should NOT be able to access the admin dashboard.
     * Links to: NFR #4
     */
    @Test
    @WithMockUser(username = "student@test.com", authorities = {"STUDENT"})
    void testStudentCannotAccessAdminDashboard() throws Exception {
        mvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden()); // Expect a 403 Forbidden error
    }

    /**
     * Requirement: A teacher should NOT be able to access the admin dashboard.
     * Links to: NFR #4
     */
    @Test
    @WithMockUser(username = "teacher@test.com", authorities = {"TEACHER"})
    void testTeacherCannotAccessAdminDashboard() throws Exception {
        mvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden()); // Expect a 403 Forbidden error
    }

    /**
     * Requirement: An admin SHOULD be able to access the admin dashboard.
     * Links to: NFR #4
     */
    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"ADMIN"})
    void testAdminCanAccessAdminDashboard() throws Exception {
        mvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk()); // Expect a 200 OK
    }

    /**
     * Requirement: A student SHOULD be able to access their own dashboard.
     */
    @Test
    @WithMockUser(username = "student@test.com", authorities = {"STUDENT"})
    void testStudentCanAccessStudentDashboard() throws Exception {
        mvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk()); // Expect a 200 OK
    }

    /**
     * Requirement: A student should NOT be able to access the teacher dashboard.
     * Links to: NFR #4
     */
    @Test
    @WithMockUser(username = "student@test.com", authorities = {"STUDENT"})
    void testStudentCannotAccessTeacherDashboard() throws Exception {
        mvc.perform(get("/teacher/dashboard"))
                .andExpect(status().isForbidden()); // Expect a 403 Forbidden
    }
}