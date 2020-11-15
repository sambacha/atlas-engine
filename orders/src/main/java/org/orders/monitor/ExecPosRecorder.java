package org.atlas.engine.financialexchange.orders.monitor;

public interface ExecPosRecorder {
	void recordExecutionPoint(String className, String methodSignature, long id, String entryExit);
}
