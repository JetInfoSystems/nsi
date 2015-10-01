package jet.isur.nsi.common.utils;

import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.common.data.NsiDataException;

import java.util.*;

public class NsiUtils {

    public static final String IS_DELETED_ATTR = "IS_DELETED";
    public static final String LAST_CHANGE_ATTR = "LAST_CHANGE";
    public static final String LAST_USER_ATTR = "LAST_USER";
    public static final String NSI_YES = "Y";
    public static final String NSI_NO = "N";

    public static String getDictAttrSingleValue(DictRowAttr attr) throws NsiDataException {
        if(attr.getValues() == null || attr.getValues().size() != 1) {
            throw new NsiDataException("Expected single value of attribute. Found: " + attr.getValues());
        } else {
            return attr.getValues().get(0);
        }
    }

    public static String getDictAttrSingleValue(DictRow row, String attrName) throws NsiDataException {
        return getDictAttrSingleValue(getDictRowAttr(row, attrName));
    }

    public static String getDictAttrSingleValue(DictRow row, NsiConfigAttr attrCfg) throws NsiDataException {
        return getDictAttrSingleValue(row, attrCfg.getName());
    }

    public static String getDictAttrFirstValue(DictRowAttr attr) {
        if(attr.getValues() == null || attr.getValues().isEmpty()) {
            return null;
        } else {
            return attr.getValues().get(0);
        }
    }

    public static String getDictAttrFirstValue(DictRow row, String attrName) throws NsiDataException {
        return getDictAttrFirstValue(getDictRowAttr(row, attrName));
    }

    public static String getDictAttrFirstValue(DictRow row, NsiConfigAttr attrCfg) throws NsiDataException {
        return getDictAttrFirstValue(getDictRowAttr(row, attrCfg.getName()));
    }

    public static DictRowAttr getDictRowId(DictRow row, NsiConfigDict dict) throws NsiDataException {
        return getDictRowAttr(row, dict.getIdAttr().getName());
    }

    public static DictRowAttr getDictRowAttr(DictRow row, String attrName) throws NsiDataException {
        if(row == null) {
            throw new NsiDataException("dict row is null");
        } else if(attrName == null) {
            throw new NsiDataException("attrName is null");
        } else if(row.getAttrs() == null) {
            throw new NsiDataException("dict row attrs list is null");
        }
        DictRowAttr attr = row.getAttrs().get(attrName);
        if(attr == null) {
            throw new NsiDataException("Attribute " + attrName + " not found in dict row");
        } else {
            return attr;
        }
    }

    public static DictRow clone(DictRow row) {
        DictRow clone = new DictRow();
        if(row.getAttrs() != null) {
            Map<String, DictRowAttr> cloneAttrs = new HashMap<>();
            for (Map.Entry<String, DictRowAttr> attrEntry : row.getAttrs().entrySet()) {
                cloneAttrs.put(attrEntry.getKey(), clone(attrEntry.getValue()));
            }
            clone.setAttrs(cloneAttrs);
        }
        return clone;
    }

    public static DictRowAttr clone(DictRowAttr attr) {
        DictRowAttr clone = new DictRowAttr();
        if(attr.getValues() != null) {
            clone.setValues(new LinkedList<>(attr.getValues()));
        }
        if(attr.getRefAttrs() != null) {
            Map<String, DictRowAttr> refAttrs = new HashMap<>();
            for (Map.Entry<String, DictRowAttr> refAttrEntry : attr.getRefAttrs().entrySet()) {
                refAttrs.put(refAttrEntry.getKey(), clone(refAttrEntry.getValue()));
            }
            clone.setRefAttrs(refAttrs);
        }
        return clone;
    }
}
