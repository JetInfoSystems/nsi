package jet.isur.nsi.services.config.test;

import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.model.builder.MetaDictBuilder;
import jet.isur.nsi.api.model.builder.MetaDictBuilderImpl;

public class DataGen {

    public static MetaDictBuilder genMetaDict(String name, String table) {
        return new MetaDictBuilderImpl()
        .name(name)
        .caption("Справочник1")
        .table(table)
        .field()
            .name("id")
            .size(19)
            .type(MetaFieldType.NUMBER)
        .add()
        .field()
            .name("is_deleted")
            .size(1)
            .type(MetaFieldType.BOOLEAN)
        .add()
        .field()
            .name("last_change")
            .type(MetaFieldType.DATE_TIME)
        .add()
        .field()
            .name("last_user")
            .size(19)
            .type(MetaFieldType.NUMBER)
        .add()
        .field()
            .name("f1")
            .size(100)
            .type(MetaFieldType.VARCHAR)
        .add()
        .attr()
            .addField("f1")
            .type(MetaAttrType.VALUE)
            .caption("f 1")
            .hidden(false)
            .name("f1")
        .add()
        .attr()
            .addField("id")
            .type(MetaAttrType.VALUE)
            .caption("id")
            .hidden(false)
            .createOnly(true)
            .name("id")
.required(true)
        .add()
        .attr()
            .addField("is_deleted")
            .type(MetaAttrType.VALUE)
            .caption("is_deleted")
            .hidden(false)
            .name("is_deleted")
        .add()
        .attr()
            .addField("last_change")
            .type(MetaAttrType.VALUE)
            .caption("last change")
            .hidden(true)
            .name("last_change")
        .add()
        .attr()
            .addField("last_user")
            .type(MetaAttrType.VALUE)
            .caption("last_user")
            .hidden(false)
            .name("last_user")
        .add()
        .idAttr("id")
        .deleteMarkAttr("is_deleted")
        .lastChangeAttr("last_change")
        .lastUserAttr("last_user")
        .addCaptionAttr("f1")
        .addRefObjectAttr("f1")
        .addLoadDataAttr("id")
        .addTableObjectAttr("f1")
        .addConstraint("constraint");
    }
}