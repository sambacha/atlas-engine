package org.atlas.engine.financialexchange.orders.monitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

@Service("asyncExecPosRecorder")
public class AsyncExecPosRecorder implements ExecPosRecorder {

	private static final Log logger = LogFactory.getLog(AsyncExecPosRecorder.class);

	private ExecPosMessageUtils execPosMessageUtils;
	private ExecutorService executorService;
	private LinkedBlockingQueue<ExecPosMessage> queue;
//	private Producer<Long, ExecPosMessage> producer;
	
	public AsyncExecPosRecorder() {
		
		execPosMessageUtils = new ExecPosMessageUtils();
//		producer = execPosMessageUtils.getProducer();
		queue = new LinkedBlockingQueue<>();
		executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "AsyncExecPosMessageRecorder");
				t.setDaemon(true);
				return t;
			}
		});
		executorService.submit(() -> {
			try {
				while (true) {
					ExecPosMessage execPosMessage = queue.poll(1000, TimeUnit.SECONDS);
					if (execPosMessage != null) {
//						producer.send(new ProducerRecord<Long, ExecPosMessage>(
//								execPosMessageUtils.getExecPosMessageTopic(), 
//								new Long(execPosMessage.getId()), execPosMessage));
						logger.trace(execPosMessage.toString());
					}
				}
			} catch(Exception e) {
				logger.error("Error while publishing exec pos message to kafka", e);
			} finally {
//				producer.close();
			}
		});
	} 	
	
	@Override
	public void recordExecutionPoint(String className, String methodSignature, long id, String entryExit) {
		queue.add(execPosMessageUtils.createMessage(className, methodSignature, id, entryExit));
	}

}
