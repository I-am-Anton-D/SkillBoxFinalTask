package main.config;

import java.io.File;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config  implements WebMvcConfigurer {

    private final String location = "static/upload";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(location + "/*")
            .addResourceLocations("file:" + location + "/");
    }

    public String getLocation() {
        return location;
    }
}
