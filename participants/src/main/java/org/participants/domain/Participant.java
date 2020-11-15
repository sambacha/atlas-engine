package org.atlas.engine.financialexchange.participants.domain;

public interface Participant {

	public ParticipantType getParticipantType();
	public Long getId();
	public void setId(Long id);
	public String getName();
	public void setName(String name);
} 
