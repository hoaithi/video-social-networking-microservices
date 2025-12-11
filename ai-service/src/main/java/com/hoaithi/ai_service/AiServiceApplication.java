package com.hoaithi.ai_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiServiceApplication {

	public static void main(String[] args) {
//		// Set Jackson constraint before Spring starts
//		System.setProperty("com.fasterxml.jackson.core.StreamReadConstraints.maxStringLength", "50000000");
		SpringApplication.run(AiServiceApplication.class, args);
	}

}
