package jet.isur.nsi.migrator;

import java.io.OutputStream;
import java.sql.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.MetaFieldType;

import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmBasicAttributeType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmColumnType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmCompositeIdType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmCompositeKeyBasicAttributeType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmConfigParameterType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmGeneratorSpecificationType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmHibernateMapping;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmManyToOneType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmRootEntityType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSimpleIdType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmTypeSpecificationType;
import org.hibernate.tuple.GenerationTiming;


public class NsiDictToHbmEntitySerializer {
    private final JAXBContext context;

    public NsiDictToHbmEntitySerializer() {
        try {
            this.context = JAXBContext.newInstance(JaxbHbmHibernateMapping.class);
        } catch (JAXBException e) {
            throw new MigratorException("build serializer", e);
        }
    }

    private JaxbHbmRootEntityType buildRootEntity(NsiConfigDict dict) {
        JaxbHbmRootEntityType result = new JaxbHbmRootEntityType();
        result.setName(dict.getName());
        result.setTable(dict.getTable());
        // id
        if(dict.getIdAttr().getFields().size() == 1) {
            result.setId(buildSimpleId(dict));
        } else {
            result.setCompositeId(buildCompositeId(dict));
        }
        // attrs
        for ( NsiConfigAttr attr : dict.getAttrs()) {
            if(attr != dict.getIdAttr()) {
                if(attr.getType() == MetaAttrType.VALUE) {
                    for ( NsiConfigField field : attr.getFields()) {
                        result.getAttributes().add(buildBasicAttribute(field));
                    }
                } else {
                    result.getAttributes().add(buildManyToOne(attr));
                }
            }
        }
        return result;
    }

    private JaxbHbmManyToOneType buildManyToOne(NsiConfigAttr attr) {
        JaxbHbmManyToOneType result = new JaxbHbmManyToOneType();
        result.setName(attr.getName());
        result.setEntityName(attr.getRefDict().getName());
        for ( NsiConfigField field : attr.getFields()) {
            result.getColumnOrFormula().add(buildColumn(field));
        }
        return result;
    }

    private JaxbHbmColumnType buildColumn(NsiConfigField field) {
        JaxbHbmColumnType result = new JaxbHbmColumnType();
        result.setName(field.getName());
        if(field.getType() == MetaFieldType.DATE_TIME) {
        	result.setLength(7);
        } else if(field.getSize() > 0) {
            result.setLength(field.getSize());
        }
        if(field.getPrecision() > 0) {
            result.setPrecision(field.getPrecision());
        }
        return result;
    }

    private JaxbHbmBasicAttributeType buildBasicAttribute(NsiConfigField field) {
        JaxbHbmBasicAttributeType result = new JaxbHbmBasicAttributeType();
        result.setName(field.getName());
        result.setType(buildTypeSpecification(field));
        result.getColumnOrFormula().add(buildColumn(field));
        result.setGenerated(GenerationTiming.NEVER);
        return result;
    }

    private JaxbHbmSimpleIdType buildSimpleId(NsiConfigDict dict) {
        JaxbHbmSimpleIdType result = new JaxbHbmSimpleIdType();
        NsiConfigField field = dict.getIdAttr().getFields().get(0);
        result.setType(buildTypeSpecification(field));
        result.getColumn().add(buildColumn(field));
        result.setGenerator(buildSequince(dict));
        return result;
    }

    private JaxbHbmTypeSpecificationType buildTypeSpecification(
            NsiConfigField field) {
        JaxbHbmTypeSpecificationType result = new JaxbHbmTypeSpecificationType();
        Class<?> fieldClass = metaFieldTypeToPOJOType(field);

        result.setName(fieldClass.getName());
        return result;
    }

    private Class<?> metaFieldTypeToPOJOType(NsiConfigField field) {
        // это отображение типов нужно только для однозначного отображения типов java на заданные типы sql
        switch (field.getType()) {
        case BOOLEAN:
            return Boolean.class;
        case CHAR:
            return char.class;
        case DATE_TIME:
            return Date.class;
        case NUMBER:
            return Long.class;
        case VARCHAR:
            return String.class;
        default:
            throw new MigratorException("Invalid field type: " + field.getType());
        }
    }

    private JaxbHbmGeneratorSpecificationType buildSequince(NsiConfigDict dict) {
        JaxbHbmGeneratorSpecificationType result = new JaxbHbmGeneratorSpecificationType();
        result.setClazz("enhanced-sequence");
        result.getConfigParameters().add(buildConfigParameter("sequence_name",dict.getSeq()));
        return result;
    }

    private JaxbHbmConfigParameterType buildConfigParameter(String name,
            String value) {
        JaxbHbmConfigParameterType result = new JaxbHbmConfigParameterType();
        result.setName(name);
        result.setValue(value);
        return result;
    }

    private JaxbHbmCompositeIdType buildCompositeId(NsiConfigDict dict) {
        JaxbHbmCompositeIdType result = new JaxbHbmCompositeIdType();
        for ( NsiConfigField field : dict.getIdAttr().getFields()) {
            result.getKeyPropertyOrKeyManyToOne().add(buildCompositeKeyBasicAttribute(field));
        }
        return result;
    }

    private JaxbHbmCompositeKeyBasicAttributeType buildCompositeKeyBasicAttribute(
            NsiConfigField field) {
        JaxbHbmCompositeKeyBasicAttributeType result = new JaxbHbmCompositeKeyBasicAttributeType();
        result.setName(field.getName());
        result.setLength(field.getSize());
        return result;
    }

    private JaxbHbmHibernateMapping buildHibernateMapping(NsiConfigDict dict) {
        JaxbHbmHibernateMapping result = new JaxbHbmHibernateMapping();
        result.getClazz().add(buildRootEntity(dict));
        return result;
    }

    public void marshalTo(NsiConfigDict dict, OutputStream os) {
        try {
            Marshaller marchaler = context.createMarshaller();
            marchaler.marshal(buildHibernateMapping(dict), os);
        } catch (JAXBException e) {
            throw new MigratorException("marchal",e);
        }
    }



}
