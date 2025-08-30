package com.virtualcard.user_service.mapper;

import org.mapstruct.Mapper;

import com.virtualcard.common.dto.UserDTO;
import com.virtualcard.common.mapper.EntityMapper;
import com.virtualcard.user_service.entity.User;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Mapper(componentModel = "spring"// ,
//unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface UserMapper extends EntityMapper<UserDTO, User> {

	/*-
	@Override
	@Mappings({
			@Mapping(target = "id", ignore = true),
			@Mapping(target = "password", ignore = true),
			@Mapping(target = "role", ignore = true),
	})
	User dtoToEntity(UserDTO dto);
	*/
}
