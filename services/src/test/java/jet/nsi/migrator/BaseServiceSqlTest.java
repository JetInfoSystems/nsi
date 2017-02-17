package jet.nsi.migrator;


import jet.nsi.api.data.BoolExpBuilder;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.model.BoolExp;
import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.PlatformDaoUtils;
import org.junit.Before;

public abstract class BaseServiceSqlTest extends BaseSqlTest {
    protected PlatformMigrator platformMigrator;

    public BaseServiceSqlTest(String dbIdent) {
        super(dbIdent);
    }

    @Before
    public void initMigrator() {
        dataSource = platformMigrator.getDataSource();
    }

    public BoolExp buildIdFilter(NsiConfigDict dict, String id){
        return new BoolExpBuilder(dict)
                .key(dict.getIdAttr().getName()).eq().value(id)
                .build();
    }
}
