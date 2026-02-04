package com.example.custom_authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CustomAuthenticationProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomAuthenticationProjectApplication.class, args);
	}

}
