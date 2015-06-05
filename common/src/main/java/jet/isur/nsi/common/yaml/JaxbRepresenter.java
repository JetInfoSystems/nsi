package jet.isur.nsi.common.yaml;

import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class JaxbRepresenter extends Representer {

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
            Object propertyValue, Tag customTag) {
        Class<?> propertyType = property.getType();
        if (propertyValue == null) {
            return null;
        } else if(Enum.class.isAssignableFrom(propertyType) && propertyType.getAnnotation(XmlEnum.class) != null) {
            Field propertyField;
            try {
                propertyField = propertyType.getDeclaredField(propertyValue.toString());
                XmlEnumValue xmlEnumValue = propertyField.getAnnotation(XmlEnumValue.class);
                if(xmlEnumValue != null) {
                    ScalarNode nodeKey = (ScalarNode) representData(property.getName());
                    return new NodeTuple(nodeKey, representData(xmlEnumValue.value()));
                } else {
                    return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                }
            } catch (Exception e) {
                throw new YAMLException("Unable to serialize enum value '" + propertyValue
                        + "' for enum class: " + propertyType.getName(),e);
            }
        } else {
            return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }
    }

}
