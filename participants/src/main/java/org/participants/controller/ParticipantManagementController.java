package org.atlas.engine.financialexchange.participants.controller;

import org.atlas.engine.financialexchange.participants.domain.APIDataResponse;
import org.atlas.engine.financialexchange.participants.domain.APIResponse;
import org.atlas.engine.financialexchange.participants.domain.Broker;
import org.atlas.engine.financialexchange.participants.domain.ExchangeException;
import org.atlas.engine.financialexchange.participants.domain.ParticipantEntry;
import org.atlas.engine.financialexchange.participants.domain.ResultCode;
import org.atlas.engine.financialexchange.participants.service.ParticipantManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParticipantManagementController {

	@Autowired
	@Qualifier("participantManagementServiceImpl")
	private ParticipantManagementService participantManagmentService;
	
	@PostMapping(value = "/finex/internal/api/participant/broker", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Long> addBroker(@RequestBody ParticipantEntry participantEntry)  {
		APIDataResponse<Long> response = new APIDataResponse<>();
		try {
			long participantId = participantManagmentService.addParticipant(participantEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PARTICIPANT_ADDED.getMessage());
			response.setData(participantId);
			response.setResponseCode(ResultCode.PARTICIPANT_ADDED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	} 

	@PutMapping(value = "/finex/internal/api/participant/broker/{participantId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Broker> updateBroker(@PathVariable("participantId") long participantId,
			@RequestBody ParticipantEntry participantEntry)  {
		APIDataResponse<Broker> response = new APIDataResponse<>();
		try {
			Broker participant = (Broker) participantManagmentService.updateParticipant(participantId, participantEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PARTICIPANT_UPDATED.getMessage());
			response.setData(participant);
			response.setResponseCode(ResultCode.PARTICIPANT_UPDATED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;

	} 

	@GetMapping(value = "/finex/api/participant/broker/{participantId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Broker> getBrokerPublic(@PathVariable("participantId") long participantId)  {
		APIDataResponse<Broker> response = getBrokerImpl(participantId);
		return response;
	} 

	@GetMapping(value = "/finex/internal/api/participant/broker/{participantId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Broker> getBrokerInternal(@PathVariable("participantId") long participantId)  {
		APIDataResponse<Broker> response = getBrokerImpl(participantId);
		return response;
	} 

	@DeleteMapping(value = "/finex/internal/api/participant/broker/{participantId}", produces = "application/json", consumes = "application/json")
	public APIResponse deleteBroker(@PathVariable("participantId") long participantId)  {
		APIResponse response = new APIResponse();
		try {
			participantManagmentService.deleteParticipant(participantId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PARTICIPANT_DELETED.getMessage());
			response.setResponseCode(ResultCode.PARTICIPANT_DELETED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	} 
	
	public APIDataResponse<Broker> getBrokerImpl(long participantId)  {
		APIDataResponse<Broker> response = new APIDataResponse<>();
		try {
			Broker participant = (Broker) participantManagmentService.getParticipant(participantId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PARTICIPANT_FOUND.getMessage());
			response.setData(participant);
			response.setResponseCode(ResultCode.PARTICIPANT_FOUND.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	} 

}
