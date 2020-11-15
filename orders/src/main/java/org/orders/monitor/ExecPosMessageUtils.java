package org.atlas.engine.financialexchange.orders.monitor;

import java.time.LocalDateTime;
import java.util.Properties;

//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.Producer;

public class ExecPosMessageUtils {

	private String execPosMessageTopic;
//	private Producer<Long, ExecPosMessage> producer;

	public ExecPosMessageUtils() {
		execPosMessageTopic = System.getProperty("exec.pos.message.topic", "OrderServiceExecPos");
		String kafkaHost = System.getProperty("kafka.host", "localhost");
		int kafkaPort = Integer.parseInt(System.getProperty("kafka.port", "9091"));
		String kafkaHostPort = kafkaHost + ":" + kafkaPort;
		
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaHostPort);
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
		props.put("value.serializer", "org.atlas.engine.financialexchange.orders.monitor.ExecPosMessageSerializer");
//		producer = new KafkaProducer<>(props);
	}
	
	public ExecPosMessage createMessage(String className, String methodSignature, long id, String entryExit) {
		
		ExecPosMessage execPosMessage = new ExecPosMessage();
		execPosMessage.setClassName(className);
		execPosMessage.setMethodSignature(methodSignature);
		execPosMessage.setEntryExit(entryExit);
		execPosMessage.setId(id);
		execPosMessage.setService("OrderService");
		execPosMessage.setThreadId(Thread.currentThread().getName());
		LocalDateTime currentTime = LocalDateTime.now();
		execPosMessage.setDateTime(currentTime);
		return execPosMessage;
	}

	public String getExecPosMessageTopic() {
		return execPosMessageTopic;
	}
	
//	public Producer<Long, ExecPosMessage> getProducer() {
//		return producer;
//	}
//	
//	public ExecPosMessage createMessage(String className, String methodSignature, long id, String entryExit) {
//		
//		ExecPosMessage execPosMessage = new ExecPosMessage();
//		execPosMessage.setClassName(className);
//		execPosMessage.setMethodSignature(methodSignature);
//		execPosMessage.setEntryExit(entryExit);
//		execPosMessage.setId(id);
//		execPosMessage.setService("OrderService");
//		execPosMessage.setThreadId(Thread.currentThread().getName());
//		LocalDateTime currentTime = LocalDateTime.now();
//		execPosMessage.setDateTime(currentTime);
//		return execPosMessage;
//	}

}
