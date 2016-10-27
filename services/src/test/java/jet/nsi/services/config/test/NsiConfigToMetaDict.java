package jet.nsi.services.config.test;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigParams;
import jet.nsi.api.model.MetaDict;
import jet.nsi.common.config.impl.NsiConfigImpl;
import jet.nsi.common.helper.MetaDictGen;

/**
 * Created by akatkevich on 23.10.2016.
 */
public class NsiConfigToMetaDict {

    @Test
    public void metaToDict() {
        // MetaDict o1 = DataGen.genMetaDict("dict1", "table1").build();
        MetaDict o = MetaDictGen.genMetaDict("dict1").build();
        NsiConfigImpl nc = new NsiConfigImpl(new NsiConfigParams());
        nc.addDict(o);
        NsiConfigDict ncd = nc.getDict("dict1");
        System.out.println(new Yaml().dump(o));
        System.out.println(new Yaml().dump(ncd));
    }
}
