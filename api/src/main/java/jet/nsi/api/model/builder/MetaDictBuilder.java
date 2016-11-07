
package jet.nsi.api.model.builder;

import java.util.List;

import jet.nsi.api.model.MetaAttrType;
import jet.nsi.api.model.MetaDict;
import jet.nsi.api.model.MetaFieldType;

public interface MetaDictBuilder {

    public interface MetaFieldBuilder {

        MetaFieldBuilder name(String value);

        MetaFieldBuilder type(MetaFieldType value);

        MetaFieldBuilder size(Integer value);

        MetaFieldBuilder precision(Integer value);

        MetaFieldBuilder defaultValue(String value);
        
        MetaDictBuilder add();
    }

    public interface MetaAttrBuilder {

        MetaAttrBuilder type(MetaAttrType value);

        MetaAttrBuilder valueType(String value);

        MetaAttrBuilder name(String value);

        MetaAttrBuilder caption(String value);

        MetaAttrBuilder addField(String value);

        MetaAttrBuilder refDict(String value);

        MetaAttrBuilder hidden(Boolean value);

        MetaAttrBuilder createOnly(Boolean value);

        MetaAttrBuilder required(Boolean value);
        
        MetaAttrBuilder refAttrHidden(boolean value);

        MetaDictBuilder add();
    }

    MetaDictBuilder name(String value);

    MetaDictBuilder caption(String value);

    MetaDictBuilder table(String value);

    MetaFieldBuilder field();

    MetaAttrBuilder attr();

    MetaDictBuilder idAttr(String name);

    MetaDictBuilder parentAttr(String name);

    MetaDictBuilder isGroupAttr(String name);

    MetaDictBuilder ownerAttr(String name);

    MetaDictBuilder deleteMarkAttr(String name);

    MetaDictBuilder lastChangeAttr(String name);

    MetaDictBuilder lastUserAttr(String name);

    MetaDictBuilder uniqueAttr(List<String> name);
    
    MetaDictBuilder addCaptionAttr(String value);

    MetaDictBuilder addRefObjectAttr(String value);

    MetaDictBuilder addTableObjectAttr(String value);

    MetaDictBuilder addMergeExternalAttr(String value);

    MetaDictBuilder addInterceptor(String value);

    MetaDictBuilder label(String value);

    MetaDictBuilder addOwn(String key, String value);

    MetaDict build();

    MetaDictBuilder addLoadDataAttr(String value);



}
