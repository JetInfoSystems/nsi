package jet.isur.nsi.testkit.test;

import java.io.File;

import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.common.sql.DefaultSqlDao;
import jet.isur.nsi.common.sql.DefaultSqlGen;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class BaseTest extends BaseSqlTest {

    protected DefaultSqlDao sqlDao;
    protected DefaultSqlGen sqlGen;
    public static DateTimeFormatter BASE_DATE_FORMATTER = DateTimeFormat.forPattern("DD.MM.YYYY");

    public void setup() throws Exception {
        super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        configParams.setLastUserDict("USER_PROFILE");
        config = new NsiConfigManagerFactoryImpl().create(new File(getProperty("database.metadata.path", "/opt/isur/database/metadata")), configParams ).getConfig();
        sqlGen = new DefaultSqlGen();
        sqlDao = new DefaultSqlDao();
        sqlDao.setSqlGen(sqlGen);

    }

    @Override
    public void cleanup() {
        cleanTestDictRows();
        super.cleanup();
    }
}
