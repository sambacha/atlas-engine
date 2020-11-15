package org.atlas.engine.financialexchange.orders.monitor;

import java.util.Map;

//import org.apache.kafka.common.serialization.Deserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ExecPosMessageDesrializer  {
//public class ExecPosMessageDesrializer implements Deserializer<ExecPosMessage> {

//	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		// Nothing to do
	}

//	@Override
	public ExecPosMessage deserialize(String topic, byte[] data) {
		ExecPosMessage message = null;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();
		objectMapper.registerModule(new JavaTimeModule());
		try {
			message = objectMapper.readValue(data, ExecPosMessage.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

//	@Override
	public void close() {
		// Nothing to do
	}

//	@Override
//	public ExecPosMessage deserialize(String topic, byte[] data) {
//		ExecPosMessage message = null;
//		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.findAndRegisterModules();
//		objectMapper.registerModule(new JavaTimeModule());
//		try {
//			message = objectMapper.readValue(data, ExecPosMessage.class);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return message;
//	}

}
