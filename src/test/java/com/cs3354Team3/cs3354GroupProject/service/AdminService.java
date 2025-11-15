package com.cs3354Team3.cs3354GroupProject.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;

/**
 * This service provides admin-level logic, like setting deadlines.
 * We are creating it here so the StudentService can compile.
 */
@Service
public class AdminService {

    // For now, we'll just hard-code a drop deadline.
    // A real implementation would read this from the database.
    private LocalDate dropDeadline = LocalDate.of(2025, 12, 1);

    /**
     * This is the method that StudentService is looking for.
     */
    public LocalDate getDropDeadline() {
        return dropDeadline;
    }

    public void setDropDeadline(LocalDate newDeadline) {
        this.dropDeadline = newDeadline;
    }
}