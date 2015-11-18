package jet.isur.nsi.services.sql.test;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.services.NsiGenericServiceImpl;
import jet.isur.nsi.services.NsiServiceImpl;
import jet.isur.nsi.services.NsiTransactionServiceImpl;
import jet.isur.nsi.testkit.test.BaseSqlTest;
import jet.scdp.metrics.mock.MockMetrics;

public class NsiServiceTest extends BaseSqlTest {

    private NsiServiceImpl nsiService;
    private NsiGenericServiceImpl nsiGenericService;
    private NsiTransactionServiceImpl transactionService;
    
    @Before
    public void setup() throws Exception {
    	super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        config = new NsiConfigManagerFactoryImpl().create(new File("/opt/isur/metadata"), configParams ).getConfig();
        nsiService = new NsiServiceImpl(new MockMetrics());
        nsiGenericService = new NsiGenericServiceImpl(new MockMetrics());
        transactionService = new NsiTransactionServiceImpl(new MockMetrics()); 
			
        transactionService.setDataSource(dataSource);
        nsiGenericService.setTransactionService(transactionService);
        nsiService.setSqlDao(sqlDao);
        nsiService.setTransactionService(transactionService);
        nsiService.setNsiGenericService(nsiGenericService);
    }
    
    @Test
    public void testMergeByExternalId() {
    	NsiConfigDict dictConf = config.getDict("EMP");
    	
    	DictRow emp = defaultBuilder("EMP")
    					.attr("EXTERNAL_ID", "1")
    					.attr("FIRST_NAME", "FIRST_NAME 1")
    					.attr("FIRST_NAME", "LAST_NAME 111s")
    					.attr("MIDDLE_NAME", "MIDDLE_NAME 1")
    					.attr("ID", "4")
    					.build();
    	
  //  	DictMergeOperation res = nsiService.dictMergeByExternalId("1111", emp, false);
   // 	System.out.println(res);
 
    }
}
