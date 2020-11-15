package org.atlas.engine.financialexchange.participants.repository;

import java.util.Optional;

import org.atlas.engine.financialexchange.participants.domain.Broker;
import org.atlas.engine.financialexchange.participants.domain.Participant;
import org.atlas.engine.financialexchange.participants.domain.ParticipantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("participantJpaRepositoryImpl")
public class ParticipantJpaRepositoryImpl implements ParticipantRepository {

	@Autowired
	private BrokerRepository brokerRepository;

	@Override
	public long saveParticipant(Participant participant) {
		long id = -1;
		try {
			if (participant.getParticipantType() == ParticipantType.BROKER) {
				Broker broker = (Broker) participant;
				brokerRepository.saveAndFlush(broker);
				Optional<Broker> savedBroker = brokerRepository.findByName(participant.getName());
				if (savedBroker.isPresent()) {
					id = savedBroker.get().getId();
				}
			}
 		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	@Override
	public boolean deleteParticipant(long participantId) {
		boolean successfullyDeleted = false;
		try {
			brokerRepository.deleteById(participantId);
			successfullyDeleted = true;
		} catch (Exception e) {
			e.printStackTrace();
			successfullyDeleted = false;
		}
		return successfullyDeleted;
	}

	@Override
	public Participant getParticipant(long participantId) {
		Participant foundParticipant = null;
		try {
			Optional<Broker> broker = brokerRepository.findById(participantId);
			if (broker.isPresent()) {
				foundParticipant = broker.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return foundParticipant;
	}

	@Override
	public int getCount() {
		int count = (int) brokerRepository.count();
		return count;
	}

}
