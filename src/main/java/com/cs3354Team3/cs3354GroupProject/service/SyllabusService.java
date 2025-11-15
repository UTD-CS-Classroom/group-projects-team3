package com.cs3354Team3.cs3354GroupProject.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SyllabusService {

    // In-memory storage: courseId -> syllabus text
    private final Map<Long, String> syllabusByCourse = new ConcurrentHashMap<>();

    public void saveSyllabusText(Long courseId, String text) {
        if (courseId == null) return;
        if (text == null) text = "";
        syllabusByCourse.put(courseId, text);
    }

    public String getSyllabusText(Long courseId) {
        if (courseId == null) return null;
        return syllabusByCourse.get(courseId);
    }
}
