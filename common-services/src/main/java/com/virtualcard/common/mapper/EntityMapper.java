package com.virtualcard.common.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.virtualcard.common.dto.DTO;
import com.virtualcard.common.springdata.entity.BaseEntity;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
public interface EntityMapper<D extends DTO, E extends BaseEntity> {

	@Mappings({
//			@Mapping(target = "version", ignore = true)
	})
	D entityToDTO(E entity);

	@Mappings({
			@Mapping(target = "id", ignore = true),
//			@Mapping(target = "version", ignore = true)
	})
	E dtoToEntity(D dto);

}
