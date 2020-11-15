package org.atlas.engine.financialexchange.participants.repository;

import org.atlas.engine.financialexchange.participants.domain.Participant;

public interface ParticipantRepository {
	long saveParticipant(Participant participant);
	boolean deleteParticipant(long participantId);
	Participant getParticipant(long participantId);
	int getCount();
}

