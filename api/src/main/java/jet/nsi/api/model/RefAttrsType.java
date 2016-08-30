package jet.nsi.api.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum RefAttrsType {
    @XmlEnumValue(value = "refObjectAttrs")
    REF_OBJECT_ATTRS,
    @XmlEnumValue(value = "mergeExternalAttrs")
    MERGE_EXTERNAL_ATTRS;
}
