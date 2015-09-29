package jet.isur.nsi.common.config.impl.test;

import java.io.File;
import java.util.Set;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.model.MetaDict;
import jet.isur.nsi.common.config.impl.NsiLocalGitConfigManagerImpl;
import jet.isur.nsi.common.config.impl.NsiYamlMetaDictReaderImpl;
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
        NsiConfigDict dict1 = config.getDict("dict1");
        Assert.assertNotNull(dict1);
        Assert.assertTrue(dict1.isHidden());
        Assert.assertTrue(dict1.getAttr("last_user").isReadonly());
    }

    private NsiLocalGitConfigManagerImpl buildConfigManager(String configPath) {
        NsiConfigParams configParams = new NsiConfigParams();
        return new NsiLocalGitConfigManagerImpl(new File(configPath), new NsiYamlMetaDictReaderImpl(), configParams );
    }

}

