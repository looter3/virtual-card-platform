package com.virtualcard.common.persistence.strategy;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;

/**
 * @author lex_looter
 *
 *         7 giu 2025
 *
 */
public class DtoSuffixStrategy extends DefaultGeneratorStrategy {

	private static final String DTO = "DTO";

	@Override
	public String getJavaClassName(final Definition definition, final Mode mode) {
		final String name = super.getJavaClassName(definition, mode);
		if (mode == Mode.POJO) {
			return name + DTO;
		}
		return name;
	}

}
