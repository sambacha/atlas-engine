package org.atlas.engine.financialexchange.participants.repository;

import java.util.Optional;

import org.atlas.engine.financialexchange.participants.domain.Broker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrokerRepository extends JpaRepository<Broker, Long> {

	public Optional<Broker> findByName(String name);

}
