package jet.isur.nsi.api.data.builder;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.model.DictRowAttr;

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

    public static DictRowAttr from(long value) {
        return from(Long.toString(value));
    }

    public static DictRowAttr from(boolean value) {
        return from(Boolean.toString(value));
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
