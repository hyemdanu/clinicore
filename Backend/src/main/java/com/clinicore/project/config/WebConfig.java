package com.clinicore.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * serves uploaded message attachments from disk.
 * cors lives in CorsConfig, not here.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads/messages}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // if a user wants to add a file/image
        registry.addResourceHandler("/uploads/messages/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
