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
	public static final String CARDS_AGGREGATE = "/cards-aggregate";
	public static final String COVERED = "/covered";
	public static final String TRANSACTIONS = "/transactions";
	public static final String UPDATE_BALANCE = "/updateBalance";
	public static final String SPEND = "/spend";
	public static final String TOPUP = "/topup";

	public static final String AMOUNT_QUERY = "?amount=";

	// Parameters
	public static final String ID = "/{id}";

	// Mapping
	public static final String GET_COVERED_CARD_MAPPING = COVERED + ID;
	public static final String SPEND_MAPPING = ID + SPEND;
	public static final String TOPUP_MAPPING = ID + TOPUP;
	public static final String UPDATE_BALANCE_MAPPING = ID + UPDATE_BALANCE;

	// URLs
	public static final String GET_CARD_URL = CARDS + SLASH;
	public static final String GET_COVERED_CARD_URL = CARDS + COVERED + SLASH;

}
