CREATE TABLE EQUITY (
	ID 			SERIAL PRIMARY KEY,
	SYMBOL		VARCHAR(48),
	DESCRIPTION VARCHAR(128)
);

CREATE TABLE BROKER (
	ID 			SERIAL PRIMARY KEY,
	NAME		VARCHAR(128)
);

CREATE TABLE TRADE (
	ID 				SERIAL PRIMARY KEY,
	TRADE_TIME		TIMESTAMP,
	BUY_TRADABLE_ID	NUMERIC(10),
	SELL_TRADABLE_ID	NUMERIC(10),
	QUANTITY	    NUMERIC(9),
	PRICE			NUMERIC(9,4),
	BUSTED			BOOLEAN DEFAULT FALSE
);

CREATE TABLE ORDERS (
	ID					SERIAL PRIMARY KEY,
	PRODUCT_ID			NUMERIC(10),
	PARTICIPANT_ID		NUMERIC(10),
	ENTRY_TIME			TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	SIDE				VARCHAR(8),
	QUANTITY	    	NUMERIC(9),
	TRADED_QUANTITY 	NUMERIC(9) DEFAULT 0,
	BOOKED_QUANTITY 	NUMERIC(9) DEFAULT 0,
	CANCELLED_QUANTITY 	NUMERIC(9) DEFAULT 0,
	PRICE				NUMERIC(9, 4),
	STATUS				VARCHAR(24) DEFAULT 'NEW',
	ORDER_TYPE			VARCHAR(16),
	LONGEVITY			VARCHAR(16)
);
