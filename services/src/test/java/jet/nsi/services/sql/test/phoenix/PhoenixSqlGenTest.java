package jet.nsi.services.sql.test.phoenix;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigParams;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.BoolExp;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.nsi.common.platform.phoenix.PhoenixNsiPlatform;
import jet.nsi.common.platform.phoenix.PhoenixPlatformSqlGen;
import jet.nsi.migrator.BaseServiceSqlTest;
import jet.nsi.migrator.platform.postgresql.PostgresqlPlatformMigrator;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

public class PhoenixSqlGenTest extends BaseServiceSqlTest {

    private static final String DB_IDENT = "postgres";

    private NsiConfig config;

    private PhoenixPlatformSqlGen phSqlGen;
    public PhoenixSqlGenTest() {
        super(DB_IDENT);
    }

    @Override
    public void setup() throws Exception {
        super.setup();

        NsiConfigParams configParams = new NsiConfigParams();
        config = new NsiConfigManagerFactoryImpl().create(new File("src/test/resources/metadata1"), configParams ).getConfig();
    }

    @Override
    protected void initPlatformSpecific() {
        phSqlGen = new PhoenixPlatformSqlGen(new PhoenixNsiPlatform());
        platformMigrator = new PostgresqlPlatformMigrator(params); //только чтобы базовый класс не упал
        platform = platformMigrator.getPlatform();
    }



    @Test
    public void testDict1RowInsertSeq() {
        NsiConfigDict dict = config.getDict("dict1");
        NsiQuery query = dict.query().addAttrs();

        String sql = phSqlGen.getRowInsertSql(query, false);
        Assert.assertEquals(
                "UPSERT INTO table1 (f1, id, is_deleted, last_change, last_user, ORG_ID, ORG_ROLE_ID, ownership_id, version) "
                        + "values (?,?,?,?,?,?,?,?,?)", sql);

    }


    @Test
    public void testDict2RowUpdateSeq() {
        NsiConfigDict dict = config.getDict("dict2");
        NsiQuery query = dict.query().addAttrs();
        BoolExp filter = buildIdFilter(dict, "id");
        String sql = phSqlGen.getRowUpdateSql(query, filter);
        Assert.assertEquals(
                "UPSERT INTO table2 (dict1_id, f1, id, is_deleted, last_change, last_user, ownership_id, version) values (?,?,?,?,?,?,?,?)", sql);
    }


}
