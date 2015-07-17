package jet.isur.nsi.testkit.utils;

import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaAttr;
import jet.isur.nsi.api.model.MetaDict;
import jet.isur.nsi.api.model.MetaField;

import org.junit.Assert;

public class DataUtils {

    public static void assertEqual(MetaDict o1, MetaDict o2) {
        Assert.assertEquals(o1.getCaption(), o2.getCaption());
        Assert.assertEquals(o1.getDeleteMarkAttr(), o2.getDeleteMarkAttr());
        Assert.assertEquals(o1.getIsGroupAttr(), o2.getIsGroupAttr());
        Assert.assertEquals(o1.getIdAttr(), o2.getIdAttr());
        Assert.assertEquals(o1.getLastChangeAttr(), o2.getLastChangeAttr());
        Assert.assertEquals(o1.getLastUserAttr(), o2.getLastUserAttr());
        Assert.assertEquals(o1.getName(), o2.getName());
        Assert.assertEquals(o1.getOwnerAttr(), o2.getOwnerAttr());
        Assert.assertEquals(o1.getOwnerDict(), o2.getOwnerDict());
        Assert.assertEquals(o1.getParentAttr(), o2.getParentAttr());
        Assert.assertEquals(o1.getTable(), o2.getTable());
        Assert.assertEquals(o1.getCaptionAttrs(), o2.getCaptionAttrs());
        Assert.assertEquals(o1.getRefObjectAttrs(), o2.getRefObjectAttrs());
        Assert.assertEquals(o1.getTableObjectAttrs(), o2.getTableObjectAttrs());
        Assert.assertEquals(o1.getFields().size(), o2.getFields().size());
        for(int i=0;i<o1.getFields().size();i++) {
            assertEquals(o1.getFields().get(i), o2.getFields().get(i));
        }
        Assert.assertEquals(o1.getAttrs().size(), o2.getAttrs().size());
        for(int i=0;i<o1.getAttrs().size();i++) {
            assertEquals(o1.getAttrs().get(i), o2.getAttrs().get(i));
        }
    }

    public static void assertEquals(MetaAttr o1, MetaAttr o2) {
        if(o1 == o2) {
            return;
        }
        Assert.assertEquals(o1.getType(), o2.getType());
        Assert.assertEquals(o1.getCaption(), o2.getCaption());
        Assert.assertEquals(o1.getName(), o2.getName());
        Assert.assertEquals(o1.getRefDict(), o2.getRefDict());
        Assert.assertEquals(o1.getValueType(), o2.getValueType());
        Assert.assertEquals(o1.getFields(), o2.getFields());
        Assert.assertEquals(o1.getHidden(), o2.getHidden());
    }

    public static void assertEquals(MetaField o1, MetaField o2) {
        if(o1 == o2) {
            return;
        }
        Assert.assertEquals(o1.getName(), o2.getName());
        Assert.assertEquals(o1.getType(), o2.getType());
        Assert.assertEquals(o1.getPrecision(), o2.getPrecision());
        Assert.assertEquals(o1.getSize(), o2.getSize());
    }

    public static void assertEquals(NsiQuery query,DictRow o1, DictRow o2) {
        if(o1 == o2) {
            return;
        }
        Assert.assertNotNull(o1);
        Assert.assertNotNull(o2);
        Assert.assertEquals(o1.getAttrs().size(), o2.getAttrs().size());
        DictRowBuilder o2Reader = new DictRowBuilder(query, o2);
        for ( String attrName : o1.getAttrs().keySet()) {
            DictRowAttr o1AttrValue = o1.getAttrs().get(attrName);
            DictRowAttr o2AttrValue = o2Reader.getAttr(attrName);
            Assert.assertNotNull(o2AttrValue);
            Assert.assertArrayEquals(o1AttrValue.getValues().toArray(), o2AttrValue.getValues().toArray());
            if(o1AttrValue.getRefAttrs() == o2AttrValue.getRefAttrs()) {
                continue;
            }
            Assert.assertNotNull(o1AttrValue.getRefAttrs());
            for ( String rk : o1AttrValue.getRefAttrs().keySet()) {
                DictRowAttr r1AttrValue = o1AttrValue.getRefAttrs().get(rk);
                DictRowAttr r2AttrValue = o2AttrValue.getRefAttrs().get(rk);
                Assert.assertNotNull(r2AttrValue);
                Assert.assertArrayEquals(r1AttrValue.getValues().toArray(), r2AttrValue.getValues().toArray());
            }
        }
    }

}
