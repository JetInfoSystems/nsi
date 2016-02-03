package jet.isur.nsi.services.config.test;

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
        DataUtils.assertEqualAllOptionals(o1, o2);
    }
    
    @Test
    public void testReadConfigFileWithClob() {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1");
        MetaDict o1 = DataGen.genMetaDictWithClob("dictWithClob", "tableWithClob").build();
        MetaDict o2 = configManager.readConfigFile(new File("src/test/resources/metadata1/clob/dictWithClob.yaml"));
        DataUtils.assertEqualCommon(o1, o2);
        DataUtils.assertEqualRefObjectAttrs(o1, o2);
        DataUtils.assertEqualTableObjectAttrs(o1, o2);
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
        
        NsiConfigDict dictWithClob = config.getDict("dictWithClob");
        Assert.assertNotNull(dictWithClob);
        Assert.assertTrue(dictWithClob.isHidden());
        Assert.assertNotNull(dictWithClob.getAttr("clobAttr"));
    }

    @Test
    public void testAddVersionToProxyModel() {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1");
        NsiConfig config = configManager.readConfig();
        Assert.assertNotNull(config);
        
        NsiConfigDict dict = config.getDict("dict1_v");
        Assert.assertNotNull(dict);
        Assert.assertNotNull(dict.getVersionAttr());
    }

    private NsiLocalGitConfigManagerImpl buildConfigManager(String configPath) {
        NsiConfigParams configParams = new NsiConfigParams();
        return new NsiLocalGitConfigManagerImpl(new File(configPath), new NsiYamlMetaDictReaderImpl(), configParams );
    }

}

