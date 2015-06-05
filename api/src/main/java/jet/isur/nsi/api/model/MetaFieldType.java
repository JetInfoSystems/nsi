package jet.isur.nsi.api.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum MetaFieldType {

    @XmlEnumValue(value = "string")
    STRING,
    @XmlEnumValue(value = "number")
    NUMBER,
    @XmlEnumValue(value = "date-time")
    DATE_TIME,
    @XmlEnumValue(value = "boolean")
    BOOLEAN;

}
