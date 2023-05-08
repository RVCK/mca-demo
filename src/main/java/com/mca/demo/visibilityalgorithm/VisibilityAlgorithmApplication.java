package com.mca.demo.visibilityalgorithm;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "Demo", version = "2.0", description = "MCA demo"))
@SpringBootApplication
public class VisibilityAlgorithmApplication {

	public static void main(String[] args) {
		SpringApplication.run(VisibilityAlgorithmApplication.class, args);
	}

}
