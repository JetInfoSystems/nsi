package jet.nsi.services.config.test;

import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigParams;
import jet.nsi.api.model.MetaDict;
import jet.nsi.common.config.impl.NsiConfigImpl;
import jet.nsi.common.helper.MetaDictGen;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by akatkevich on 23.10.2016.
 */
public class NsiConfigToMetaDict {

    @Test
    public void metaToDict() {
        MetaDict o1 = DataGen.genMetaDict("dict1", "table1").build();
        MetaDict o2 = MetaDictGen.genMetaDict("Тестовый объект BOM").build();
        NsiConfigImpl nc = new NsiConfigImpl(new NsiConfigParams());
        nc.addDict(o1);
        NsiConfigDict ncd = nc.getDict("dict1");
        System.out.println(new Yaml().dump(o1));
        System.out.println(new Yaml().dump(ncd));
    }
}
