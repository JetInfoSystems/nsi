package jet.nsi.api.model;

import java.beans.Transient;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.ConvertUtils;
import jet.nsi.api.data.NsiConfigAttr;

import com.google.common.base.Strings;

/**
 * Значение атрибута справочника
 */
public class DictRowAttr implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Значение полей, если полей несколько то порядок значений соответствует порядку в описании атрибута в метаданных
     */
    private List<String> values;
    /**
     * Если атрибут является ссылкой, то для него будут предоставлены атрибуты представляющие refObject.
     */
    private Map<String, DictRowAttr> refAttrs;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void addValue(String value) {
        if(values == null) {
            values = new LinkedList<>();
        }
        values.add(value);
    }

    public Map<String, DictRowAttr> getRefAttrs() {
        return refAttrs;
    }

    public void setRefAttrs(Map<String, DictRowAttr> refAttrs) {
        this.refAttrs = refAttrs;
    }

    @Transient
    public boolean isEmpty() {
        if(values == null || values.isEmpty()) {
            return true;
        } else {
            for ( String v : values) {
                if(!Strings.isNullOrEmpty(v)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isEmpty(DictRowAttr attr) {
        return attr == null || attr.isEmpty();
    }

    @Transient
    private String getOneValue() {
        if(isEmpty()) {
            return null;
        }
        if(values.size() == 1) {
            return values.get(0);
        } else {
            throw new NsiServiceException("attr has no one value: %s",values);
        }
    }

    @Transient
    public String getString() {
        return getOneValue();
    }

    @Transient
    public Boolean getBoolean() {
        return ConvertUtils.stringToBool(getOneValue());
    }

    @Transient
    public Integer getInteger() {
        return ConvertUtils.stringToInteger(getOneValue());
    }

    @Transient
    public Long getLong() {
        return ConvertUtils.stringToLong(getOneValue());
    }

    @Transient
    public DateTime getDateTime() {
        return ConvertUtils.stringToDateTime(getOneValue());
    }

    @Transient
    public Double getDouble() {
        return ConvertUtils.stringToDouble(getOneValue());
    }

    @Override
    public String toString() {
        return "DictRowAttr [values=" + values + ", refAttrs=" + refAttrs + "]";
    }

    public static String formatAttrs(List<NsiConfigAttr> configAttrs, Map<String, DictRowAttr> rowAttrs, String sep) {
        StringBuilder res = new StringBuilder();
        for(NsiConfigAttr attr : configAttrs) {
            DictRowAttr ra = rowAttrs.get(attr.getName());
            if(ra != null) {
                res.append(ra.getValues()).append(sep);
            }
        }
        
        return res.toString();
    }
    
}