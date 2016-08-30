package jet.nsi.common.yaml;

import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class JaxbConstructor extends CustomClassLoaderConstructor {

    public JaxbConstructor(Class<? extends Object> theRoot,
            ClassLoader theLoader) {
        super(checkJaxbRoot(theRoot), theLoader);
        yamlClassConstructors.put(NodeId.scalar, new JaxbScalarConstructor());
    }

    protected class JaxbScalarConstructor extends ConstructScalar {
        @SuppressWarnings("unchecked")
        @Override
        public Object construct(Node nnode) {
            ScalarNode node = (ScalarNode) nnode;
            Class type = node.getType();
            // обрабатываем только enum с аннотацией @XmlEnum
            if (Enum.class.isAssignableFrom(type) && type.getAnnotation(XmlEnum.class) != null) {
                String enumValueName = node.getValue();
                try {
                    for (Field declaredField : type.getDeclaredFields()) {
                        XmlEnumValue xmlEnumValue = declaredField.getAnnotation(XmlEnumValue.class);
                        if(xmlEnumValue.value().equals(enumValueName)) {
                            return Enum.valueOf(type, declaredField.getName());
                        }
                    }
                    throw new YAMLException("Unable to find enum value '" + enumValueName
                            + "' for enum class: " + type.getName());

                } catch (Exception ex) {
                    throw new YAMLException("Unable to find enum value '" + enumValueName
                            + "' for enum class: " + type.getName());
                }
            } else {
                return super.construct(nnode);
            }
        }
    }


    private static Class<? extends Object> checkJaxbRoot(
            Class<? extends Object> theRoot) {
        // TODO Auto-generated method stub
        return theRoot;
    }

}
