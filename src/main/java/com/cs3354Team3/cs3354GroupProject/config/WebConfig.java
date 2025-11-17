package com.cs3354Team3.cs3354GroupProject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // must align with SYLLABI_UPLOAD_DIR in TeacherController
        String uploadPath = System.getProperty("user.home") + "/syllabi-uploads/";

        registry.addResourceHandler("/syllabi/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
