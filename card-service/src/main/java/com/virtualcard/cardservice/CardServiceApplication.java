package com.virtualcard.cardservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@ComponentScan("com.virtualcard")
public class CardServiceApplication {

	public static void main(final String[] args) {
		SpringApplication.run(CardServiceApplication.class, args);
	}

}
