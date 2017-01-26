package jet.nsi.migrator.platform.phoenix;


import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.migrator.platform.DefaultDictToHbmSerializer;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSimpleIdType;

public class PhoenixDictToHbmSerializer extends DefaultDictToHbmSerializer {


    protected JaxbHbmSimpleIdType buildSimpleId(NsiConfigDict dict) {
        JaxbHbmSimpleIdType result = new JaxbHbmSimpleIdType();
        NsiConfigField field = dict.getIdAttr().getFields().get(0);
        result.setType(buildTypeSpecification(field));
        result.getColumn().add(buildIdColumn(field, dict));
        return result;
    }
}
