package com.virtualcard.cardservice.mapper;

import org.mapstruct.Mapper;

import com.virtualcard.cardservice.entity.Card;
import com.virtualcard.common.dto.CardDTO;
import com.virtualcard.common.mapper.EntityMapper;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Mapper(componentModel = "spring")
public interface CardMapper extends EntityMapper<CardDTO, Card> {
}
