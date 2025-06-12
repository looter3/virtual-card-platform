package com.virtualcard.card_aggregate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = "com.virtualcard.card_aggregate")
public class App {

	public static void main(final String[] args) {
		SpringApplication.run(App.class, args);
	}

}
