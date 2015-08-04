package jet.isur.nsi.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import junit.framework.Assert;

public class DictDependencyGraphTest {

    private NsiConfig config;

    @Before
    public void setup() {
        File configPath = new File("/opt/isur/database/metadata");
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
    }

    private NsiConfigDict getDict(String name) {
        return config.getDict(name);
    }

    @Test
    public void testWORJ_TYPE() {
        DictDependencyGraph graph = DictDependencyGraph.build(config, Arrays.asList(getDict("WORK_TYPE")));
        List<NsiConfigDict> sortedDicts = graph.sort();
        Assert.assertFalse(graph.hasCycles());
        Assert.assertEquals("WORK_TYPE", sortedDicts.get(0).getName());
    }

    @Test
    public void testEVENT() {
        DictDependencyGraph graph = DictDependencyGraph.build(config, Arrays.asList(getDict("EMP")));
        List<NsiConfigDict> sortedDicts = graph.sort();
        Assert.assertFalse(graph.hasCycles());

        List<String> sortedDictNames = new ArrayList<>();
        for (NsiConfigDict dict : sortedDicts) {
            sortedDictNames.add(dict.getName());
        }

        Assert.assertEquals(
                Arrays.asList(
        "WORK_TYPE",
        "TECH_MEAN_TYPE",
        "STAFF_TYPE",
        "PARAM",
        "EVENT_TYPE",
        "PARAM_EVENT_TYPE",
        "OBJ_TYPE",
        "PARAM_EVENT_OBJ_TYPE",
        "ORG_FORM",
        "FIAS_ADDROBJ",
        "ACTIVITY_TYPE",
        "ORG",
        "ORG_UNIT",
        "ORG_OBJ",
        "ORG_COR",
        "EMP",
        "ORG_COR_EMP",
        "OBJ_PARAM",
        "OBJ_PARAM_VALUE",
        "ERT",
        "ERT_WORK_TYPE",
        "ERT_STAFF",
        "ERT_OBJ",
        "ERT_EQUIPMENT_TYPE",
        "EQUIPMENT_GROUP",
        "ERT_EQUIPMENT",
        "ERT_EQUIPMENT_MEAN",
        "ERT_EMP",
        "EMP_PHONE",
        "EMP_EMAIL"),sortedDictNames);
    }
}
