package com.virtualcard.edgeserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
public class App {

	public static void main(final String[] args) {
		SpringApplication.run(App.class, args);
	}

}
