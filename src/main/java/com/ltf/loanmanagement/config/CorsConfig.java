package com.ltf.loanmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Local-demo CORS policy: the dashboard.html is opened as a static file / served by
 * VS Code's Live Server on a different origin (e.g. http://127.0.0.1:5500) than the
 * API (http://localhost:8080), so the browser blocks fetch() calls between them
 * unless the server explicitly allows it. Wide open ("*") is fine for a local demo
 * project — do NOT ship this as-is to a real deployment.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
