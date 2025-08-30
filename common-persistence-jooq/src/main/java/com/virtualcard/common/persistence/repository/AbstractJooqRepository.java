package com.virtualcard.common.persistence.repository;

import org.jooq.DSLContext;

import lombok.RequiredArgsConstructor;

/**
 * @author Lorenzo Leccese
 *
 *         12 giu 2025
 *
 */
@RequiredArgsConstructor
public abstract class AbstractJooqRepository {

	protected final DSLContext dsl;

}
