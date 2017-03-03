package jet.nsi.common.helper;

import jet.nsi.api.model.MetaAttrType;
import jet.nsi.api.model.MetaFieldType;
import jet.nsi.api.model.builder.MetaDictBuilder;
import jet.nsi.api.model.builder.MetaDictBuilderImpl;

/**
 * Created by akatkevich on 23.10.2016.
 */
public class MetaDictGen {

    public static MetaDictBuilder genMetaDict(String name) {
        return new MetaDictBuilderImpl()
                .name(name)
                .caption("")
                .table(name)

                //Fields
                .field()
                    .name("id")
                    .size(19)
                    .type(MetaFieldType.NUMBER)
                    .add()
                .field()
                    .name("is_deleted")
                    .size(1)
                    .type(MetaFieldType.BOOLEAN)
                    .defaultValue("N")
                    .add()
                .field()
                    .name("last_change")
                    .type(MetaFieldType.DATE_TIME)
                    .add()
                .field()
                    .name("last_user")
                    .size(19)
                    .type(MetaFieldType.VARCHAR)
                    .add()
                .field()
                    .name("ownership_id")
                    .size(19)
                    .type(MetaFieldType.NUMBER)
                    .add()

                //Attrs
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
                    .type(MetaAttrType.REF)
                    .refDict("SYS_USERS")
                    .caption("last_user")
                    .hidden(false)
                    .name("last_user")
                    .add()
                .attr()
                    .addField("ownership_id")
                    .type(MetaAttrType.REF)
                    .refDict("SYS_OWNERSHIPS")
                    .caption("Владелец записи")
                    .hidden(true)
                    .createOnly(true)
                    .required(true)
                    .readOnly(true)
                    .name("ownership_id")
                    .add()

                //Others
                .idAttr("id")
                .deleteMarkAttr("is_deleted")
                .lastChangeAttr("last_change")
                .lastUserAttr("last_user")

                ;
    }
}
