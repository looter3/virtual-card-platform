package com.virtualcard.common.persistence.strategy;

import java.util.List;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;

/**
 * @author Lorenzo Leccese
 *
 *         7 giu 2025
 *
 */
public class CustomGeneratorStrategy extends DefaultGeneratorStrategy {

	private static final String DTO = "DTO";

	@Override
	public String getJavaClassName(final Definition definition, final Mode mode) {
		final String name = super.getJavaClassName(definition, mode);
		if (mode == Mode.POJO) {
			return name + DTO;
		}
		return name;
	}

	@Override
	public List<String> getJavaClassImplements(final Definition definition, final Mode mode) {
		if (definition instanceof TableDefinition && mode != Mode.POJO && mode != Mode.RECORD) {
			// TODO use reflection
			return List.of("com.virtualcard.common.persistence.entity.JooqBaseEntity");
		}
		return super.getJavaClassImplements(definition, mode);
	}

}
