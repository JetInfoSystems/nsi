package jet.nsi.services.config.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;

import jet.nsi.api.NsiMetaDictWriter;
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
        File ff = new File("src/test/resources/metadata1/labels/dictWithLabels.yaml");
        System.out.println("delete "+ff.delete());
    }

    @Test
    public void testCreateOrUpdateConfig() throws URISyntaxException {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1/empty");
        configManager.getConfig();
        int beforeSize = configManager.getConfig().getDicts().size();
        MetaDict o1 = DataGen.genMetaDict("writeDict","writeTestTable").build();
        configManager.createOrUpdateConfig(o1);

        File testFile = new File("src/test/resources/metadata1/empty/", o1.getName().concat(".yaml"));
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
    public void testReloadConfig() throws IOException {
        String testConfigPath = "src/test/resources/metadata1/empty";
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager(testConfigPath);
        NsiConfig config = configManager.readConfig();
        Assert.assertNotNull(config);

        int beforeSize = configManager.getConfig().getDicts().size();

        MetaDict o1 = DataGen.genMetaDict("writeDictForReload","writeTestTableForReload").build();
        String fileName = o1.getName().concat(".yaml");
        File newFile = new File(testConfigPath, fileName);
        try (FileWriter newFileWriter = new FileWriter(newFile)) {
            NsiMetaDictWriter writer = new NsiYamlMetaDictWriterImpl();
            writer.write(o1, newFileWriter);
        }

        NsiConfig reloadedConfig = configManager.reloadConfig();
        NsiConfig newConfig = configManager.getConfig();

        Assert.assertNotNull(reloadedConfig);
        Assert.assertNotNull(newConfig);
        Assert.assertEquals(reloadedConfig, newConfig);
        Assert.assertEquals(beforeSize + 1, configManager.getConfig().getDicts().size());

        Files.deleteIfExists(Paths.get(testConfigPath, fileName));
    }

    @Test
    public void testCheckoutNewConfig() throws IOException {
        String testConfigPath = "src/test/resources/metadata1/empty";
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager(testConfigPath);
        NsiConfig config = configManager.readConfig();
        Assert.assertNotNull(config);

        int beforeSize = configManager.getConfig().getDicts().size();

        // Готовим данные
        MetaDict o1 = DataGen.genMetaDict("writeDictForCheckout","writeTestTableForCheckout").build();
        String fromSource = "src/test/resources/metadata1/from";

        Files.createDirectory(Paths.get(fromSource));

        String fileName = o1.getName().concat(".yaml");
        File newFile = new File(fromSource, fileName);
        try (FileWriter newFileWriter = new FileWriter(newFile)) {
            NsiMetaDictWriter writer = new NsiYamlMetaDictWriterImpl();
            writer.write(o1, newFileWriter);
        }

        configManager.checkoutNewConfig(fromSource);

        Path checkFilePath = Paths.get(testConfigPath, fileName);
        Assert.assertTrue(Files.exists(checkFilePath));

        Files.deleteIfExists(Paths.get(fromSource, fileName));
        Files.deleteIfExists(Paths.get(fromSource));
        Files.deleteIfExists(checkFilePath);
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

