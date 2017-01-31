package jet.nsi.migrator;


import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.PlatformDaoUtils;
import org.junit.Before;

public abstract class BaseServiceSqlTest extends BaseSqlTest {
    protected PlatformMigrator platformMigrator;

    public BaseServiceSqlTest(String dbIdent, PlatformDaoUtils daoUtils) {
        super(dbIdent, daoUtils);
    }

    @Before
    public void initMigrator() {
        dataSource = platformMigrator.getDataSource();
    }
}
