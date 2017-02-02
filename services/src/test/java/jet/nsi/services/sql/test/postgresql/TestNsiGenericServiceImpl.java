/*
package jet.nsi.services.sql.test.postgresql;

import jet.metrics.mock.MockMetrics;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.migrator.platform.postgresql.PostgresqlPlatformMigrator;
import jet.nsi.services.NsiGenericServiceImpl;
import jet.nsi.services.NsiTransactionServiceImpl;
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.PostgresqlPlatformDaoUtils;

public class TestNsiGenericServiceImpl extends BaseSqlTest {

    private static final String DB_IDENT = "nsi.postgresql95";

    private NsiConfig config;
    private NsiGenericServiceImpl service;
    private NsiTransactionServiceImpl transactionService;
    private PlatformMigrator platformMigrator;

    public TestNsiGenericServiceImpl() {
        super(DB_IDENT, new PostgresqlPlatformDaoUtils());
    }
    
    @Override
    public void setup() throws Exception {
        super.setup();
        
        transactionService = new NsiTransactionServiceImpl(new MockMetrics());
//        transactionService.setDataSource(dataSource);
        service = new NsiGenericServiceImpl(new MockMetrics());
        service.setTransactionService(transactionService);
    }

    @Override
    protected void initPlatformSpecific() {
        platformMigrator = new PostgresqlPlatformMigrator(params);
//        platformMigrator.setParams(params);
        platform = platformMigrator.getPlatform();
    }

}
*/
