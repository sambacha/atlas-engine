package org.atlas.engine.financialexchange.participants.domain;

import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseParticipantImpl implements Participant {

	private static AtomicLong idGenerator = new AtomicLong(1);

	private long id;
	private String name;
	
	public BaseParticipantImpl() {
		this.id = idGenerator.getAndIncrement();
	}
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
}
