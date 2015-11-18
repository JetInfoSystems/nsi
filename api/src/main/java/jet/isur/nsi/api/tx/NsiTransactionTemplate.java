package jet.isur.nsi.api.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jet.isur.nsi.api.NsiServiceException;

public class NsiTransactionTemplate<T> {
	private NsiTransactionService transactionService;
	String requestId;
	
	public NsiTransactionTemplate(NsiTransactionService transactionService, String requestId) {
		this.transactionService = transactionService;
		this.requestId = requestId;
	}
	
	public T execute(NsiTransactionCallback callback) {
		try (NsiTransaction tx = transactionService.createTransaction(requestId)) {
			try {
				return (T) callback.doInTransaction(tx);
			} catch (Exception e) {
				callback.onException(requestId, e);	            
	            tx.rollback();
	            throw new NsiServiceException(e.getMessage());
			}
		} catch (Exception e) {
			callback.onException(requestId, e);
            throw new NsiServiceException(e.getMessage());
		}
	}
}
