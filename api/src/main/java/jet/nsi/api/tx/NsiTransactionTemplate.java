package jet.nsi.api.tx;

import org.slf4j.Logger;

import jet.nsi.api.NsiServiceException;

import java.sql.Connection;

public abstract class NsiTransactionTemplate<T> {
	private NsiTransactionService transactionService;
	String requestId;
	Logger log;
	Connection connection;
	
	public NsiTransactionTemplate(NsiTransactionService transactionService, Connection connection, String requestId, Logger log) {
		this.transactionService = transactionService;
		this.requestId = requestId;
		this.log = log;
		this.connection = connection;
	}
	
	public abstract T doInTransaction(NsiTransaction tx);
	
	public T start() {
		try (NsiTransaction tx = transactionService.createTransaction(requestId, connection)) {
			try {
				return (T) doInTransaction(tx);
			} catch (Throwable e) {
				log.error("onException rollback [{}] -> error", requestId, e);            
	            tx.rollback();
	            throw new NsiServiceException(e.getMessage());
			}
		} catch (Exception e) {
			log.error("onException [{}] -> error", requestId, e);
            throw new NsiServiceException(e.getMessage());
		}
	}
}
