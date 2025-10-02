package com.example.wide;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Wide Demo Application",
                version = "1.0",
                description = "API documentation for Wide Demo Spring Boot project"
        )
)
public class WideApplication {
	public static void main(String[] args) {
		SpringApplication.run(WideApplication.class, args);
	}
}
