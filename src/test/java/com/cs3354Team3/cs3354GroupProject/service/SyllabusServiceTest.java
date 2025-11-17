package com.cs3354Team3.cs3354GroupProject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SyllabusServiceTest {

    private SyllabusService syllabusService;

    // This method runs before each test, ensuring a fresh start
    @BeforeEach
    void setUp() {
        // We create a new instance of the service for each test
        // This ensures tests don't interfere with each other
        syllabusService = new SyllabusService();
    }

    @Test
    void testSaveAndGetSyllabus_HappyPath() {
        // --- Arrange ---
        Long courseId = 1L;
        String syllabusText = "This is the official syllabus.";

        // --- Act ---
        syllabusService.saveSyllabusText(courseId, syllabusText);
        String retrievedText = syllabusService.getSyllabusText(courseId);

        // --- Assert ---
        // Check that the text we saved is the same as the text we got back
        assertEquals(syllabusText, retrievedText);
    }

    @Test
    void testGetSyllabus_WhenNotFound() {
        // --- Act ---
        // Try to get a syllabus for an ID that was never saved
        String retrievedText = syllabusService.getSyllabusText(99L);

        // --- Assert ---
        // The service's 'get' method should return null for a non-existent ID
        assertNull(retrievedText);
    }

    @Test
    void testSaveSyllabus_WithNullCourseId() {
        // --- Act ---
        // Try to save with a null courseId. The method should just return.
        syllabusService.saveSyllabusText(null, "Some text");

        // --- Assert ---
        // We can't easily check the internal map, but we can verify that
        // no syllabus was saved under a "null" key (which would be bad)
        // and that calling get with 'null' still returns null (as per its own logic).
        assertNull(syllabusService.getSyllabusText(null));
    }

    @Test
    void testSaveSyllabus_WithNullText() {
        // --- Arrange ---
        Long courseId = 2L;

        // --- Act ---
        // The service should convert null text to an empty string
        syllabusService.saveSyllabusText(courseId, null);
        String retrievedText = syllabusService.getSyllabusText(courseId);

        // --- Assert ---
        // Verify that the retrieved text is an empty string, not null
        assertNotNull(retrievedText);
        assertEquals("", retrievedText);
    }

    @Test
    void testGetSyllabus_WithNullCourseId() {
        // --- Act ---
        // Try to get with a null courseId
        String retrievedText = syllabusService.getSyllabusText(null);

        // --- Assert ---
        // The method should return null, as per its guard clause
        assertNull(retrievedText);
    }

    @Test
    void testUpdateSyllabus() {
        // --- Arrange ---
        Long courseId = 3L;
        String originalText = "Old syllabus version 1.";
        String updatedText = "New syllabus version 2.";

        // --- Act ---
        // Save the first version
        syllabusService.saveSyllabusText(courseId, originalText);
        String retrievedOriginal = syllabusService.getSyllabusText(courseId);

        // Save the second version (updating the existing entry)
        syllabusService.saveSyllabusText(courseId, updatedText);
        String retrievedUpdated = syllabusService.getSyllabusText(courseId);

        // --- Assert ---
        // Check both steps
        assertEquals(originalText, retrievedOriginal, "Original text should be saved correctly");
        assertEquals(updatedText, retrievedUpdated, "Updated text should overwrite the original");
    }
}