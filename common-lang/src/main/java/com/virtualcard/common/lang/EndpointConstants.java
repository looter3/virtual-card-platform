package com.virtualcard.common.lang;

import static com.virtualcard.common.lang.LangConstants.SLASH;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
public class EndpointConstants {

	// Context root
	public static final String CARDS = "/cards";
	public static final String USER = "/user";
	public static final String CARDS_AGGREGATE = "/cards-aggregate";
	public static final String COVERED = "/covered";
	public static final String CREDENTIALS = "/credentials";
	public static final String TRANSACTIONS = "/transactions";
	public static final String UPDATE_BALANCE = "/updateBalance";
	public static final String BALANCE_OPERATION = "/balanceOperation";
	public static final String SPEND = "/spend";
	public static final String TOPUP = "/topup";
	public static final String GET_ALL_CARDS_BY_USER = "/getAllCardsByUser";
	public static final String LAST_MONTH = "/thisMonth";

	public static final String AMOUNT_QUERY = "?amount=";

	// Parameters
	public static final String ID = "/{id}";
	public static final String CARD_NUMBER = "/{cardNumber}";
	public static final String USERNAME = "/{username}";

	// Mapping
	public static final String GET_COVERED_CARD_MAPPING = COVERED + CARD_NUMBER;
	public static final String GET_CREDENTIALS__BY_USER = CREDENTIALS + USERNAME;
	public static final String GET_TRANSACTIONS_LAST_MONTH_BY_CARD_ID = LAST_MONTH + ID;
	public static final String GET_ALL_CARDS_BY_USER_MAPPING = GET_ALL_CARDS_BY_USER + USERNAME;
	public static final String SPEND_MAPPING = ID + SPEND;
	public static final String TOPUP_MAPPING = ID + TOPUP;
	public static final String UPDATE_BALANCE_MAPPING = ID + UPDATE_BALANCE;

	// URLs
	public static final String GET_CARD_URL = CARDS + SLASH;
	public static final String GET_COVERED_CARD_URL = CARDS + COVERED + SLASH;

}
