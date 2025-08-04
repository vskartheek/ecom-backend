package com.ecommerce.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SbEcomApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext=SpringApplication.run(SbEcomApplication.class, args);
	}

}
