package jet.nsi.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.nsi.common.data.DictDependencyGraph;
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
    public void testDICT1() {
        DictDependencyGraph graph = DictDependencyGraph.build(config, Arrays.asList(getDict("DICT1")));
        List<NsiConfigDict> sortedDicts = graph.sort();
        Assert.assertFalse(graph.hasCycles());
        Assert.assertEquals("DICT1", sortedDicts.get(0).getName());
    }

    @Test
    public void testDICT4() {
        DictDependencyGraph graph = DictDependencyGraph.build(config, Arrays.asList(getDict("DICT4")));
        List<NsiConfigDict> sortedDicts = graph.sort();
        Assert.assertFalse(graph.hasCycles());

        List<String> sortedDictNames = new ArrayList<>();
        for (NsiConfigDict dict : sortedDicts) {
            sortedDictNames.add(dict.getName());
        }

        Assert.assertEquals(
                Arrays.asList(
                         "DICT3",
                         "DICT1",
                         "DICT2",
                         "DICT4"), sortedDictNames);
    }

    private void getConfiguration() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("project.properties");
        Properties props = new Properties();
        props.load(in);
        metadataPath = "src/test/resources/metadata";
    }

}
