package org.atlas.engine.financialexchange.participants.service;

import org.atlas.engine.financialexchange.participants.domain.ExchangeException;
import org.atlas.engine.financialexchange.participants.domain.Participant;
import org.atlas.engine.financialexchange.participants.domain.ParticipantEntry;

public interface ParticipantManagementService {
	long addParticipant(ParticipantEntry participantEntry) throws ExchangeException;
	Participant getParticipant(long participantId) throws ExchangeException;
	void deleteParticipant(long participantId) throws ExchangeException;
	Participant updateParticipant(long participantId, ParticipantEntry participantEntry) throws ExchangeException;
}

