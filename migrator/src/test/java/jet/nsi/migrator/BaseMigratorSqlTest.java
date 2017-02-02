package jet.nsi.migrator;


import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.PlatformDaoUtils;
import org.junit.Before;

public abstract class BaseMigratorSqlTest extends BaseSqlTest {
    protected PlatformMigrator platformMigrator;

    public BaseMigratorSqlTest(String dbIdent) {
        super(dbIdent);
    }

    @Before
    public void initMigrator() {
        dataSource = platformMigrator.getDataSource();
    }
}
