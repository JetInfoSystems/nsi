package jet.nsi.testkit.test;

import java.io.File;

import jet.nsi.api.data.NsiConfigParams;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class BaseTest extends BaseSqlTest {

    public static DateTimeFormatter BASE_DATE_FORMATTER = DateTimeFormat.forPattern("DD.MM.YYYY");

    public void setup() throws Exception {
        super.setup();
        NsiConfigParams configParams = new NsiConfigParams();
        configParams.setLastUserDict("USER_PROFILE");
        config = new NsiConfigManagerFactoryImpl().create(new File(getProperty("database.nsi.metadata.path", "/opt/nsi/database/metadata")), configParams ).getConfig();
    }

    @Override
    public void cleanup() {
        cleanTestDictRows();
        super.cleanup();
    }
}
