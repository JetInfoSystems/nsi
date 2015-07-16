package jet.isur.nsi.common.config.impl.test;

import java.io.File;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.testkit.test.BaseSqlTest;
import junit.framework.Assert;

import org.junit.Test;

public class DatabaseMetadataTest extends BaseSqlTest {

    @Test
    public void testCheckDatabaseMetadata() {
        File configPath = new File(getProperty("database.metadata.path","/opt/isur/database/metadata"));
        NsiConfigParams configParams = new NsiConfigParams();
        configParams.setLastUserDict("USER_PROFILE");
        NsiConfigManager configManager = new NsiConfigManagerFactoryImpl().create(configPath, configParams );
        NsiConfig config = configManager.getConfig();
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getDict("ORG"));
        Assert.assertEquals("В работе", config.getDict("EVENT").getField("STATE").getEnumValues().get("1"));
        Assert.assertEquals("ALLOW", config.getDict("ACL_ROLE_PERMISSION").getField("ACL_MODE").getEnumValues().get("A"));
    }

}

