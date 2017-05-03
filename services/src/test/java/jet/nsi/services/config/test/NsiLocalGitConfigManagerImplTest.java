package jet.nsi.services.config.test;

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
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

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
    public void testCreateOrUpdateConfig() throws URISyntaxException {
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager("src/test/resources/metadata1/empty");
        configManager.getConfig();
        int beforeSize = configManager.getConfig().getDicts().size();
        MetaDict o1 = DataGen.genMetaDict("writeDict", "writeTestTable").build();
        File testFile = new File("src/test/resources/metadata1/empty/", o1.getName().concat(".yaml"));
        try {
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
        } finally {
        /*delete test file to prevent configManager to read it next time*/
            testFile.delete();
        }

    }

    @Test
    public void testReloadConfig() throws IOException {
        String testConfigPath = "src/test/resources/metadata1/empty";
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager(testConfigPath);
        NsiConfig config = configManager.readConfig();
        Assert.assertNotNull(config);

        int beforeSize = configManager.getConfig().getDicts().size();

        MetaDict o1 = DataGen.genMetaDict("writeDictForReload", "writeTestTableForReload").build();
        String fileName = o1.getName().concat(".yaml");
        try {
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
        } finally {
            Files.deleteIfExists(Paths.get(testConfigPath, fileName));
        }
    }

    @Test
    public void testCheckoutNewConfig() throws IOException {
        String testConfigPath = "src/test/resources/metadata1/empty";
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager(testConfigPath);
        NsiConfig config = configManager.readConfig();
        Assert.assertNotNull(config);


        // Готовим данные
        MetaDict o1 = DataGen.genMetaDict("writeDictForCheckout", "writeTestTableForCheckout").build();

        String fromSource = "src/test/resources/metadata1/from";
        String fileName = o1.getName().concat(".yaml");
        Path checkFilePath = null;
        try {
            Files.createDirectory(Paths.get(fromSource));

            File newFile = new File(fromSource, fileName);
            try (FileWriter newFileWriter = new FileWriter(newFile)) {
                NsiMetaDictWriter writer = new NsiYamlMetaDictWriterImpl();
                writer.write(o1, newFileWriter);
            }

            configManager.checkoutNewConfig(fromSource);

            checkFilePath = Paths.get(testConfigPath, fileName);
            Assert.assertTrue(Files.exists(checkFilePath));
        } finally {
            Files.deleteIfExists(Paths.get(fromSource, fileName));
            Files.deleteIfExists(Paths.get(fromSource));
            Files.deleteIfExists(checkFilePath);
        }
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

    @Test
    public void testReadConfigFileWithPaths() {
        String configPath = "src/test/resources/metadata-by-dirs";
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager(configPath);
        configManager.readConfig();

        MetaDict metaDict1 = configManager.getConfig().getMetaDict("dict_in_a");
        MetaDict metaDict2 = configManager.getConfig().getMetaDict("dict_in_b");

        Path pathA = configManager.getConfig().getMetaDictPath("dict_in_a");
        Path pathB = configManager.getConfig().getMetaDictPath("dict_in_b");

        Assert.assertNotNull(metaDict1);
        Assert.assertNotNull(metaDict2);
        Assert.assertEquals("dict_in_a migth be read with path to ${config-folder}/a", Paths.get(configPath, "a").resolve(metaDict1.getName() + ".yaml").toString(), pathA.toString());
        Assert.assertEquals("dict_in_b migth be read with path to ${config-folder}/b", Paths.get(configPath, "b").resolve(metaDict2.getName() + ".yaml").toString(), pathB.toString());
    }

    @Test
    public void testWriteConfigFileWithPaths() throws IOException {
        String configPath = "src/test/resources/empty";
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager(configPath);
        configManager.readConfig();

        int beforeSize = configManager.getConfig().getMetaDicts().size();

        // Create new writeDict
        String name = "writeDict";
        try {
            MetaDict o1 = DataGen.genMetaDict(name, name + "Table").build();
            configManager.createOrUpdateConfig(o1, "a");


            Path path1 = Paths.get(configPath, "a").resolve(name + ".yaml");

            MetaDict o2 = configManager.readConfigFile(path1.toFile());

            Path path2 = configManager.getConfig().getMetaDictPath(name);
            MetaDict o3 = configManager.getConfig().getMetaDict(name);

            Assert.assertEquals(path1.toString(), path2.toString());
            DataUtils.assertEqualAllOptionals(o1, o2);
            DataUtils.assertEqualAllOptionals(o1, o3);
            Assert.assertEquals(beforeSize + 1, configManager.getConfig().getMetaDicts().size());

            // Update writeDict and change it path
            o1.setCaption("New write test");
            configManager.createOrUpdateConfig(o1, "c");

            path1 = Paths.get(configPath, "c").resolve(name + ".yaml");
            path2 = configManager.getConfig().getMetaDictPath(name);
            Assert.assertEquals(path1.toString(), path2.toString());

            o3 = configManager.getConfig().getMetaDict(name);
            DataUtils.assertEqualAllOptionals(o1, o3);
            Assert.assertEquals(beforeSize + 1, configManager.getConfig().getMetaDicts().size());

            // Re-read config to check if it will be successfull (dublicates on File system is not exists)
            configManager.readConfig();
            Assert.assertEquals(beforeSize + 1, configManager.getConfig().getMetaDicts().size());
        } finally {

            Files.deleteIfExists(configManager.getConfig().getMetaDictPath(name));
            Files.delete(Paths.get(configPath, "c"));
            Files.delete(Paths.get(configPath, "a"));
        }

    }

    @Test
    public void testCreateRefConfig() throws IOException {
        String configPath = "src/test/resources/metadata1/ref";
        NsiLocalGitConfigManagerImpl configManager = buildConfigManager(configPath);
        configManager.readConfig();
        MetaDict o3 = configManager.getConfig().getMetaDict("testRef");
        configManager.createOrUpdateConfig(o3);
    }

    private NsiLocalGitConfigManagerImpl buildConfigManager(String configPath) {
        NsiConfigParams configParams = new NsiConfigParams();
        return new NsiLocalGitConfigManagerImpl(new File(configPath), new NsiYamlMetaDictReaderImpl(), new NsiYamlMetaDictWriterImpl(), configParams);
    }

}

