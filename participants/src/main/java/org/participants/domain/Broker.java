package org.atlas.engine.financialexchange.participants.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "broker")
public class Broker implements Participant, Serializable {

	private static final long serialVersionUID = -3009157732242241606L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "name")
	private String name;
	
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

	public String toString() {
		StringBuffer message = new StringBuffer();
		message.append("id = ").append(getId()).append(";");
		message.append("name = ").append(getName());
		return message.toString();
	}

	@Override
	public ParticipantType getParticipantType() {
		return ParticipantType.BROKER;
	}
	
}
