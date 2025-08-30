package com.virtualcard.common.configuration;

import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.context.annotation.Bean;

import jakarta.persistence.Persistence;

/**
 * @author Lorenzo Leccese
 *
 *         10 ago 2025
 *
 */
public abstract class SpringServiceConfiguration {

	@Bean
	Mutiny.SessionFactory sessionFactory() {
		return Persistence.createEntityManagerFactory("default") // name from persistence.xml
			.unwrap(Mutiny.SessionFactory.class);
	}

}
