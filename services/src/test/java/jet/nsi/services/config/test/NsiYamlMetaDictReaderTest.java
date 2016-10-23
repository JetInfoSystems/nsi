package jet.nsi.services.config.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jet.nsi.api.model.MetaDict;
import jet.nsi.common.config.impl.NsiYamlMetaDictReaderImpl;
import jet.nsi.common.yaml.JaxbRepresenter;
import jet.nsi.testkit.utils.DataUtils;

import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class NsiYamlMetaDictReaderTest {

    @Test
    public void testDumpRead() throws Exception {
        MetaDict o1 = DataGen.genMetaDict("dict1", "table1").build();

     /*   DumperOptions options = new DumperOptions();
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
*/


        String dump = new Yaml(new JaxbRepresenter()).dump(o1);

        System.out.println(dump);


        NsiYamlMetaDictReaderImpl reader = new NsiYamlMetaDictReaderImpl();
        try(InputStream stream = new ByteArrayInputStream(dump.getBytes(StandardCharsets.UTF_8))) {
            MetaDict o2 = reader.read(stream);

            DataUtils.assertEqualAllOptionals(o1, o2);
        }
    }

}
