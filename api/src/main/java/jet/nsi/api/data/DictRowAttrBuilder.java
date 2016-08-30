package jet.nsi.api.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import jet.nsi.api.model.DictRowAttr;

public class DictRowAttrBuilder {


    private final DictRowBuilder owner;
    private DictRowAttr prototype;
    private String name;

    DictRowAttrBuilder(DictRowBuilder owner) {
        this.owner = owner;
    }

    public DictRowAttrBuilder value(String value) {
        prototype.getValues().add(value);
        return this;
    }

    public DictRowAttrBuilder value(List<String> value) {
        prototype.getValues().addAll(value);
        return this;
    }

    public DictRowAttrBuilder refAttrs(Map<String, DictRowAttr> value) {
        prototype.setRefAttrs(value);
        return this;
    }

    public DictRowBuilder add() {
        owner.getPrototype().getAttrs().put(name, prototype);
        return owner;
    }

    void setPrototype(String name, DictRowAttr prototype) {
        this.name = name;
        this.prototype = prototype;
        // когда приступаем к редактированию атрибута нужно сбросить предыдущее значение
        prototype.getValues().clear();
    }

    public static DictRowAttr from(String value) {
        DictRowAttr result = new DictRowAttr();
        List<String> values = new ArrayList<>(1);
        result.setValues(values);
        values.add(value);
        return result;
    }

    public static DictRowAttr from(Integer value) {
        return from(ConvertUtils.integerToString(value));
    }

    public static DictRowAttr from(Long value) {
        return from(ConvertUtils.longToString(value));
    }

    public static DictRowAttr from(Double value) {
        return from(ConvertUtils.doubleToString(value));
    }

    public static DictRowAttr from(DateTime value) {
        return from(ConvertUtils.dateTimeToString(value));
    }

    public static DictRowAttr from(Boolean value) {
        return from(ConvertUtils.boolToString(value));
    }

    public static DictRowAttr build(List<String> values) {
        DictRowAttr result = new DictRowAttr();
        result.setValues(values);
        return result;
    }

    public static DictRowAttr build(String value) {
        ArrayList<String> values = new ArrayList<>(1);
        values.add(value);
        return build(values);
    }
}
