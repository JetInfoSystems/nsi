package jet.isur.nsi.common.config.impl.test;

import java.io.File;
import java.util.Set;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.model.MetaDict;
import jet.isur.nsi.common.config.impl.NsiLocalGitConfigManagerImpl;
import jet.isur.nsi.common.config.impl.NsiYamlMetaDictReaderImpl;
import jet.isur.nsi.testkit.utils.DataGen;
import jet.isur.nsi.testkit.utils.DataUtils;
import junit.framework.Assert;

import org.junit.Test;

public class NsiLocalGitConfigManagerImplTest {

    @Test
    public void testFindConfigFiles() {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1");

        Set<File> configFiles = configManager.findFiles();
        Assert.assertTrue(configFiles.contains(new File("src/test/resources/metadata1/dict1.yaml")));
        Assert.assertTrue(configFiles.contains(new File("src/test/resources/metadata1/folder1/dict2.yaml")));
    }

    @Test
    public void testReadConfigFile() {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1");
        MetaDict o1 = DataGen.genMetaDict("dict1", "table1").build();
        MetaDict o2 = configManager.readConfigFile(new File("src/test/resources/metadata1/dict1.yaml"));
        DataUtils.assertEqual(o1, o2);
    }

    @Test
    public void testGetConfig() {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1");
        NsiConfig config = configManager.readConfig();
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getDict("dict1"));
    }

    private NsiLocalGitConfigManagerImpl buildConfigManager(String configPath) {
        return new NsiLocalGitConfigManagerImpl(new File(configPath), new NsiYamlMetaDictReaderImpl());
    }

    @Test
    public void testCheckMetadata2() {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata2");
        NsiConfig config = configManager.readConfig();
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getDict("ORG"));
        Assert.assertEquals("Черновик", config.getDict("EVENT").getField("STATE").getEnumValues().get("1"));
        Assert.assertEquals("ALLOW", config.getDict("ACL_ROLE_PERMISSION").getField("ACL_MODE").getEnumValues().get("A"));
    }

}

