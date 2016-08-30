package jet.nsi.api.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum MetaAttrType {

    @XmlEnumValue(value = "value")
    VALUE,
    @XmlEnumValue(value = "ref")
    REF;

}
