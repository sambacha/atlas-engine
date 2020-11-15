package org.atlas.engine.financialexchange.participants.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.atlas.engine.financialexchange.participants.domain.Participant;
import org.springframework.stereotype.Service;

@Service("participantMemoryRepositoryImpl")
public class ParticipantMemoryRepositoryImpl implements ParticipantRepository {

	private static AtomicLong idGenerator = new AtomicLong(1);

	Map<Long, Participant> participants;
	
	public ParticipantMemoryRepositoryImpl() {
		participants = new ConcurrentHashMap<Long, Participant>();
	}
	
	@Override
	public long saveParticipant(Participant participant) {
		if (participant.getId() == null) {
			participant.setId(idGenerator.getAndIncrement());
		}
		participants.put(participant.getId(), participant);
		return participant.getId();
	}

	@Override
	public boolean deleteParticipant(long participantId) {
		Participant participant = participants.get(participantId);
		return participant != null;
	}

	@Override
	public Participant getParticipant(long participantId) {
		return participants.get(participantId);
	}

	@Override
	public int getCount() {
		return participants.size();
	}

	
}
