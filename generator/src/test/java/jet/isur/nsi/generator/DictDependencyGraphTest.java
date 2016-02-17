package jet.isur.nsi.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.isur.nsi.common.data.DictDependencyGraph;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class DictDependencyGraphTest {

    private NsiConfig config;
    private String metadataPath;

    @Before
    public void setup() throws IOException {
        getConfiguration();

        File configPath = new File(metadataPath);
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
    }

    private NsiConfigDict getDict(String name) {
        return config.getDict(name);
    }

    @Test
    public void testWORK_TYPE() {
        DictDependencyGraph graph = DictDependencyGraph.build(config, Arrays.asList(getDict("WORK_TYPE")));
        List<NsiConfigDict> sortedDicts = graph.sort();
        Assert.assertFalse(graph.hasCycles());
        Assert.assertEquals("WORK_TYPE", sortedDicts.get(0).getName());
    }

    @Test
    public void testEMP() {
        DictDependencyGraph graph = DictDependencyGraph.build(config, Arrays.asList(getDict("EMP")));
        List<NsiConfigDict> sortedDicts = graph.sort();
        Assert.assertFalse(graph.hasCycles());

        List<String> sortedDictNames = new ArrayList<>();
        for (NsiConfigDict dict : sortedDicts) {
            sortedDictNames.add(dict.getName());
        }

        Assert.assertEquals(
                Arrays.asList(
                         "ORG_FORM", 
                         "FIAS_ADDROBJ", 
                         "ACTIVITY_TYPE", 
                         "ORG", 
                         "ORG_UNIT", 
                         "OBJ_TYPE", 
                         "PARAM", 
                         "TECH_MEAN_TYPE", 
                         "ERT", 
                         "EQUIPMENT_GROUP", 
                         "EVENT_TYPE", 
                         "ORG_OBJ", 
                         "OBJ_PARAM", 
                         "WORK_TYPE", 
                         "STAFF_TYPE", 
                         "ERT_EQUIPMENT_TYPE", 
                         "ERT_EQUIPMENT", 
                         "EMP", 
                         "ORG_COR", 
                         "PARAM_EVENT_TYPE", 
                         "PARAM_EVENT_OBJ_TYPE", 
                         "OBJ_PARAM_VALUE", 
                         "ERT_WORK_TYPE", 
                         "ERT_STAFF", 
                         "ERT_EQUIPMENT_MEAN", 
                         "ERT_EMP", 
                         "ERT_OBJ", 
                         "ORG_COR_EMP", 
                         "EMP_PHONE", 
                         "EMP_EMAIL"),sortedDictNames);
    }

    private void getConfiguration() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("project.properties");
        Properties props = new Properties();
        props.load(in);
        metadataPath = "src/test/resources/metadata";
    }

}
