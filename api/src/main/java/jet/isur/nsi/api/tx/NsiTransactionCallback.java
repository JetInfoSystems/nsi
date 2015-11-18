package jet.isur.nsi.api.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NsiTransactionCallback<T> {
    private static Logger log = LoggerFactory.getLogger(NsiTransactionCallback.class);
    
	public abstract T doInTransaction(NsiTransaction tx);
	
	public void onException(String requestId, Exception e) {
		log.error("onException [{}] -> error", requestId, e);
	}
}
