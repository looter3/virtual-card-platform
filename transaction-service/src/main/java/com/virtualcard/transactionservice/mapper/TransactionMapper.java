package com.virtualcard.transactionservice.mapper;

import org.mapstruct.Mapper;

import com.virtualcard.common.dto.TransactionDTO;
import com.virtualcard.common.mapper.EntityMapper;
import com.virtualcard.transactionservice.entity.Transaction;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper extends EntityMapper<TransactionDTO, Transaction> {
}
