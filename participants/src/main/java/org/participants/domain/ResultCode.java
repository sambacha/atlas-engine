package org.atlas.engine.financialexchange.participants.domain;

public enum ResultCode {

	PARTICIPANT_ADDED(2001, "Participant added"),
	PARTICIPANT_REJECTED(2002, "Participant rejected"),
	PARTICIPANT_FOUND(2003, "Participant found"),
	PARTICIPANT_NOT_FOUND(2004, "Participant not found"),
	PARTICIPANT_UPDATED(2005, "Participant updated"),
	PARTICIPANT_DELETED(2006, "Participant deleted"),
	UNSUPPORTED_ENTITY(9000, "Operation on an unsupported entity"),
	GENERAL_ERROR(999999, "General error. Contact Exchange");
	
	ResultCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	private String message;
	private int code;
	
	public int getCode() {
		return this.code;
	}
	
	public String getMessage() {
		return this.message;
	}
}

