package jet.nsi.services.config.test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigParams;
import jet.nsi.api.model.MetaDict;
import jet.nsi.common.config.impl.NsiLocalGitConfigManagerImpl;
import jet.nsi.common.config.impl.NsiYamlMetaDictReaderImpl;
import jet.nsi.common.config.impl.NsiYamlMetaDictWriterImpl;
import jet.nsi.testkit.utils.DataUtils;
import junit.framework.Assert;

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
    public void testReadConfigFileWithLabels() {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1");
        MetaDict o1 = DataGen.genMetaDictWithLabels("dictWithLabels", "tableWithLabels").build();
        MetaDict o2 = configManager.readConfigFile(new File("src/test/resources/metadata1/labels/dict_labels.yaml"));
        DataUtils.assertEqualCommon(o1, o2);
        DataUtils.assertEqualRefObjectAttrs(o1, o2);
        DataUtils.assertEqualTableObjectAttrs(o1, o2);
        DataUtils.assertEqualLabels(o1, o2);
    }
    
    @Test
    public void testCreateOrUpdateConfig() {
        File testFile = new File("src/test/resources/metadata1/empty/writeDict.yaml");
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1/empty");
        configManager.getConfig();
        int beforeSize = configManager.getConfig().getDicts().size();
        MetaDict o1 = DataGen.genMetaDict("writeDict","writeTestTable").build();
        configManager.createOrUpdateConfig(o1);
        MetaDict o2 = configManager.readConfigFile(testFile);
        DataUtils.assertEqualAllOptionals(o1, o2);
        //It's added exactly ones
        int afterSize = configManager.getConfig().getDicts().size();
        Assert.assertEquals(beforeSize + 1, afterSize);

        //Check if it updates Correct
        o1.setCaption("New caption Проверка русского языка");
        configManager.createOrUpdateConfig(o1);
        //It's updated and not duplicates
        afterSize = configManager.getConfig().getDicts().size();
        Assert.assertEquals(beforeSize + 1, afterSize);
        o2 = configManager.readConfigFile(testFile);
        DataUtils.assertEqualAllOptionals(o1, o2);
        /*delete test file to prevent configManager to read it next time*/
        testFile.delete();
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
        return new NsiLocalGitConfigManagerImpl(new File(configPath), new NsiYamlMetaDictReaderImpl(), new NsiYamlMetaDictWriterImpl(), configParams );
    }

}

